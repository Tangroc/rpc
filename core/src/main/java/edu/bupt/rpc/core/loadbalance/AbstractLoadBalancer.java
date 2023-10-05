package edu.bupt.rpc.core.loadbalance;

import edu.bupt.rpc.core.remote.dto.RpcRequest;
import org.springframework.util.CollectionUtils;

import java.util.List;

public abstract class AbstractLoadBalancer implements LoadBalance{
    @Override
    public String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest) {
        if (CollectionUtils.isEmpty(serviceUrlList)){
            return null;
        }
        if (serviceUrlList.size()==1){
            return serviceUrlList.get(0);
        }
        return select(serviceUrlList,rpcRequest);
    }

    protected abstract String select(List<String> serviceUrlList,RpcRequest rpcRequest);
}
