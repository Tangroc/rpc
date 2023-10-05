package edu.bupt.rpc.core.loadbalance;

import edu.bupt.rpc.core.remote.dto.RpcRequest;

import java.util.List;

public interface LoadBalance {
    /**
     * 负载均衡
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
