package edu.bupt.rpc.core.registry;

import edu.bupt.rpc.core.remote.dto.RpcRequest;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {
    /**
     * 服务发现
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
