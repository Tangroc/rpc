package edu.bupt.rpc.core.remote.transport.netty.client;

import edu.bupt.rpc.common.enums.CompressTypeEnum;
import edu.bupt.rpc.common.enums.SerializationTypeEnum;
import edu.bupt.rpc.common.factory.SingletonFactory;
import edu.bupt.rpc.core.provider.impl.ZkServiceProviderImpl;
import edu.bupt.rpc.core.registry.ServiceDiscovery;
import edu.bupt.rpc.core.registry.zk.ZkServiceDiscoveryImpl;
import edu.bupt.rpc.core.remote.constant.RpcConstant;
import edu.bupt.rpc.core.remote.dto.RpcMessage;
import edu.bupt.rpc.core.remote.dto.RpcRequest;
import edu.bupt.rpc.core.remote.dto.RpcResponse;
import edu.bupt.rpc.core.remote.transport.RpcRequestTransport;
import edu.bupt.rpc.core.remote.transport.netty.codec.RpcMessageDecoder;
import edu.bupt.rpc.core.remote.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class NettyRpcClient implements RpcRequestTransport {

    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;

    public NettyRpcClient(){
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0,5,0, TimeUnit.SECONDS));
                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new NettyRpcClientHandler());

                    }
                });
        this.serviceDiscovery = SingletonFactory.getInstance(ZkServiceDiscoveryImpl.class);
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress){
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()){
                log.info("The client has connect [{}] successfully",inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            }else {
                throw new IllegalArgumentException();
            }
        });
        return completableFuture.get();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()){
            unprocessedRequests.put(rpcRequest.getRequestId(),resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.KYRO.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstant.REQUEST_TYPE)
                    .build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener)future->{
                if (future.isSuccess()){
                    log.info("client send message:[{}] successfully",rpcMessage);
                }else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("send rpc message failed:",future.cause());
                }
            });
        }else {
            throw new IllegalArgumentException();
        }
        return resultFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
    }
}
