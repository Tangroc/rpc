package edu.bupt.rpc.core.provider.impl;

import edu.bupt.rpc.common.factory.SingletonFactory;
import edu.bupt.rpc.core.config.RpcServiceConfig;
import edu.bupt.rpc.core.provider.ServiceProvider;
import edu.bupt.rpc.core.registry.ServiceRegistry;
import edu.bupt.rpc.core.registry.zk.ZkServiceRegistryImpl;
import edu.bupt.rpc.core.remote.constant.RpcConstant;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = SingletonFactory.getInstance(ZkServiceRegistryImpl.class);
    }

    @Override
    public void addService(RpcServiceConfig config) {
        String rpcServiceName = config.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, config.getService());
        log.info("Add service: {} and interfaces:{}", rpcServiceName, config.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (null == service) {
            throw new RuntimeException("无服务");
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig config) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(config);
            serviceRegistry.registryService(config.getRpcServiceName(), new InetSocketAddress(host, RpcConstant.NETTY_SERVER_PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}
