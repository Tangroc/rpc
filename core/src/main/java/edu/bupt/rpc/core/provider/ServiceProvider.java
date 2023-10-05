package edu.bupt.rpc.core.provider;

import edu.bupt.rpc.core.config.RpcServiceConfig;

public interface ServiceProvider {

    void addService(RpcServiceConfig config);

    Object getService(String rpcServiceName);

    void publishService(RpcServiceConfig config);
}
