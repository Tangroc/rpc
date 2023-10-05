package edu.bupt.rpc.core.remote.transport.netty.client;

import edu.bupt.rpc.common.enums.CompressTypeEnum;
import edu.bupt.rpc.common.enums.SerializationTypeEnum;
import edu.bupt.rpc.common.factory.SingletonFactory;
import edu.bupt.rpc.core.proxy.RpcClientProxy;

import edu.bupt.rpc.core.remote.dto.RpcMessage;
import edu.bupt.rpc.core.remote.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import edu.bupt.rpc.core.remote.constant.RpcConstant;

import java.net.InetSocketAddress;

/**
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。《Netty进阶之路 跟着案例学 Netty》
 */
@Slf4j
public class NettyRpcClientHandler extends SimpleChannelInboundHandler {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        log.info("client receive msg:[{}]",o);
        if (o instanceof RpcMessage){
            RpcMessage rpcMessage = (RpcMessage) o;
            byte messageType = rpcMessage.getMessageType();
            if (messageType == RpcConstant.HEARTBEAT_RESPONSE_TYPE){
                log.info("heart message [{}]",rpcMessage.getData());
            }else if(messageType ==  RpcConstant.RESPONSE_TYPE) {
                RpcResponse<Object> response = (RpcResponse<Object>) rpcMessage.getData();
                unprocessedRequests.complete(response);
            }
        }
    }

    // 发送心跳包，保持连接
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstant.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstant.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }

}
