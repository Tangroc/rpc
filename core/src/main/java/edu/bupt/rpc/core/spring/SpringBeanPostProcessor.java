package edu.bupt.rpc.core.spring;

import edu.bupt.rpc.common.factory.SingletonFactory;
import edu.bupt.rpc.core.annotation.RpcReference;
import edu.bupt.rpc.core.annotation.RpcService;
import edu.bupt.rpc.core.config.RpcServiceConfig;
import edu.bupt.rpc.core.provider.ServiceProvider;
import edu.bupt.rpc.core.provider.impl.ZkServiceProviderImpl;
import edu.bupt.rpc.core.proxy.RpcClientProxy;
import edu.bupt.rpc.core.remote.transport.RpcRequestTransport;
import edu.bupt.rpc.core.remote.transport.netty.client.NettyRpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 利用bean的生命周期管理rpc服务
 * <b>@RpcService</b> 标记服务提供方
 * <b>@RpcReference</b> 标记使用rpc获取远程服务
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor(){
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        this.rpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)){
            log.info("find rpc service- [{}]",bean.getClass().getName());
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean)
                    .build();
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for(Field f:declaredFields){
            RpcReference rpcReference = f.getAnnotation(RpcReference.class);
            if (null!=rpcReference){
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version())
                        .build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(f.getType());
                f.setAccessible(true);
                try{
                    f.set(bean,clientProxy);
                }catch (IllegalAccessException e){
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
