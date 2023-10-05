package edu.bupt.rpc.core.remote.handler;

import edu.bupt.rpc.common.factory.SingletonFactory;
import edu.bupt.rpc.core.provider.ServiceProvider;
import edu.bupt.rpc.core.provider.impl.ZkServiceProviderImpl;
import edu.bupt.rpc.core.remote.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 处理rpc请求
 */
@Slf4j
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;
    public RpcRequestHandler(){
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    public Object handle(RpcRequest rpcRequest){
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest,service);
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try{
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParamTypes());
            result = method.invoke(service,rpcRequest.getParameters());
            log.info("service:[{}] successfully invoke method:[{}]",rpcRequest.getInterfaceName(),rpcRequest.getMethodName());
        }catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
            throw  new RuntimeException("执行目标方法失败",e);
        }
        return result;
    }
}
