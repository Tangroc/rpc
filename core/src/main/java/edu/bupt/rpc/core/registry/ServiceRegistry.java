package edu.bupt.rpc.core.registry;

import java.net.InetSocketAddress;

public interface ServiceRegistry {
    /**
     * 服务注册
     */
    void registryService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
