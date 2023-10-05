package edu.bupt.rpc.core.remote.transport.netty.server;

import edu.bupt.rpc.common.enums.CompressTypeEnum;
import edu.bupt.rpc.common.enums.RpcResponseCodeEnum;
import edu.bupt.rpc.common.enums.SerializationTypeEnum;
import edu.bupt.rpc.common.factory.SingletonFactory;
import edu.bupt.rpc.core.remote.constant.RpcConstant;
import edu.bupt.rpc.core.remote.dto.RpcMessage;
import edu.bupt.rpc.core.remote.dto.RpcRequest;
import edu.bupt.rpc.core.remote.dto.RpcResponse;
import edu.bupt.rpc.core.remote.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义处理器处理来自客户端的数据
 */
@Slf4j
public class NettyRpcServerHandler extends SimpleChannelInboundHandler {

    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RpcMessage) {
            log.info("server receive message:[{}]", msg);
            byte messageType = ((RpcMessage) msg).getMessageType();
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
            rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
            if (messageType == RpcConstant.HEARTBEAT_REQUEST_TYPE) {
                rpcMessage.setMessageType(RpcConstant.HEARTBEAT_RESPONSE_TYPE);
                rpcMessage.setData(RpcConstant.PONG);
            } else {
                RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                Object result = rpcRequestHandler.handle(rpcRequest);
                log.info("server calc result:[{}] ", result.toString());
                rpcMessage.setMessageType(RpcConstant.RESPONSE_TYPE);
                if (ctx.channel().isActive() && ctx.channel().isActive()) {
                    RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                    rpcMessage.setData(rpcResponse);
                } else {
                    RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                    rpcMessage.setData(rpcResponse);
                    log.error("not writable now, message dropped");
                }
            }
            ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }

}
