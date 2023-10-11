package edu.bupt.rpc.core.registry.zk;

import edu.bupt.rpc.core.registry.ServiceRegistry;

import java.net.InetSocketAddress;

public class ZkServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registryService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorUtils.createNode(CuratorUtils.getZkClient(),servicePath,false);
    }
}
