package edu.bupt.rpc.core.remote.transport.socket;

import edu.bupt.rpc.common.factory.SingletonFactory;
import edu.bupt.rpc.common.utils.ThreadPoolFactoryUtil;
import edu.bupt.rpc.core.config.CustomShutdownHook;
import edu.bupt.rpc.core.config.RpcServiceConfig;
import edu.bupt.rpc.core.provider.ServiceProvider;
import edu.bupt.rpc.core.provider.impl.ZkServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

@Slf4j
public class SocketRpcServer {
    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;

    public SocketRpcServer(){
        threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-rpc-server-pool");
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    public void registerService(RpcServiceConfig rpcServiceConfig){
        serviceProvider.publishService(rpcServiceConfig);
    }

    public void start(){
        try(ServerSocket server = new ServerSocket()){
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(hostAddress,9999));
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket;
            while ((socket = server.accept())!=null){
                log.info("client connected [{}]",socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandler(socket));
            }
            threadPool.shutdown();
        }catch (IOException e){
            log.error("occur IOException:",e);
        }
    }
}
