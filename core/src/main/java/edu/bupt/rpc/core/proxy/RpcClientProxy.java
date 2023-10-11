package edu.bupt.rpc.core.proxy;

import edu.bupt.rpc.core.config.RpcServiceConfig;
import edu.bupt.rpc.core.remote.dto.RpcRequest;
import edu.bupt.rpc.core.remote.dto.RpcResponse;
import edu.bupt.rpc.core.remote.transport.RpcRequestTransport;
import edu.bupt.rpc.core.remote.transport.netty.client.NettyRpcClient;
import edu.bupt.rpc.core.remote.transport.socket.SocketRpcClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RpcClientProxy implements InvocationHandler {

    // 发送rpc请求
    private final RpcRequestTransport rpcRequestTransport;

    // rpc请求方法的配置 指定版本
    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceConfig rpcServiceConfig) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    /**
     * 获取代理对象
     * @param clazz 接口
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class<?>[]{clazz},this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoke method : [{}]",method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        RpcResponse<Object> rpcResponse = null;
        if (rpcRequestTransport instanceof NettyRpcClient){
            CompletableFuture<RpcResponse<Object>> future = (CompletableFuture<RpcResponse<Object>>) rpcRequestTransport.sendRpcRequest(rpcRequest);
            rpcResponse = future.get();
        }

        if (rpcRequestTransport instanceof SocketRpcClient){
            rpcResponse = (RpcResponse<Object>) rpcRequestTransport.sendRpcRequest(rpcRequest);
        }
        postInvoke(rpcResponse,rpcRequest);
        assert rpcResponse != null;
        return rpcResponse.getData();
    }

    private void postInvoke(RpcResponse<Object> rpcResponse,RpcRequest rpcRequest){
        if (rpcResponse == null){
            log.warn("this call result is null , requestId [{}]",rpcRequest.getRequestId());
        }
    }
}
