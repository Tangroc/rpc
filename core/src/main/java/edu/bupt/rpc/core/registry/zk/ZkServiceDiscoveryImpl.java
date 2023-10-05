package edu.bupt.rpc.core.registry.zk;

import edu.bupt.rpc.common.factory.SingletonFactory;
import edu.bupt.rpc.core.loadbalance.LoadBalance;
import edu.bupt.rpc.core.loadbalance.impl.RandomLoadBalancer;
import edu.bupt.rpc.core.registry.ServiceDiscovery;
import edu.bupt.rpc.core.remote.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {

    LoadBalance loadBalance = SingletonFactory.getInstance(RandomLoadBalancer.class);

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (CollectionUtils.isEmpty(serviceUrlList)) {
            log.warn("no service for [{}]", rpcServiceName);
            return null;
        }
        String serviceAddress = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("Successfully found the service address:[{}]", serviceAddress);
        String[] socketAddressArray = serviceAddress.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
