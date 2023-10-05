package edu.bupt.rpc.core.loadbalance.impl;

import edu.bupt.rpc.core.loadbalance.AbstractLoadBalancer;
import edu.bupt.rpc.core.remote.dto.RpcRequest;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected String select(List<String> serviceUrlList, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceUrlList.get(random.nextInt(serviceUrlList.size()));
    }
}
