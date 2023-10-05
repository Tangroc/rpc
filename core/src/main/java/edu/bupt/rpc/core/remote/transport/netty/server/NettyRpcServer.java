package edu.bupt.rpc.core.remote.transport.netty.server;

import edu.bupt.rpc.common.factory.SingletonFactory;
import edu.bupt.rpc.common.utils.ThreadPoolFactoryUtil;
import edu.bupt.rpc.core.config.CustomShutdownHook;
import edu.bupt.rpc.core.config.RpcServiceConfig;
import edu.bupt.rpc.core.provider.ServiceProvider;
import edu.bupt.rpc.core.provider.impl.ZkServiceProviderImpl;
import edu.bupt.rpc.core.remote.constant.RpcConstant;
import edu.bupt.rpc.core.remote.transport.netty.codec.RpcMessageDecoder;
import edu.bupt.rpc.core.remote.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NettyRpcServer {
    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);

    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    public void start(){
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        EventLoopGroup bossGroup = new NioEventLoopGroup(RpcConstant.NETTY_SERVER_BOSS_THREADS);
        EventLoopGroup workerGroup = new NioEventLoopGroup(RpcConstant.NETTY_SERVER_WORKER_THREADS);
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                Runtime.getRuntime().availableProcessors() * 2,
                ThreadPoolFactoryUtil.createThreadFactory("netty-rpc-server-pool", false)
        );
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            ServerBootstrap serverBootstrap = new ServerBootstrap().group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 启用TCP Nagle算法，尽可能发送大数据块，减少网络传输
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 开启TCP底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 设置TCP已连接队列最大长度
                    .option(ChannelOption.SO_BACKLOG, RpcConstant.NETTY_SERVER_TCP_CONNECTED_MAX_NUM)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    //初始化处理流水线
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // 60s 没有收到客户端发来的请求关闭连接
                            pipeline.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new RpcMessageEncoder());
                            pipeline.addLast(new RpcMessageDecoder());
                            pipeline.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });
            // 同步等待绑定端口成功
            ChannelFuture future = serverBootstrap.bind(host, RpcConstant.NETTY_SERVER_PORT).sync();
            // 等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        }catch (UnknownHostException | InterruptedException e){
            e.printStackTrace();
        }finally {
            log.info("shutdown server groups");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }
}
