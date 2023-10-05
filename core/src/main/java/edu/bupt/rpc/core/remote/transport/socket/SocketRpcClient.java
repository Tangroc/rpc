package edu.bupt.rpc.core.remote.transport.socket;

import edu.bupt.rpc.core.registry.ServiceDiscovery;
import edu.bupt.rpc.core.registry.zk.ZkServiceDiscoveryImpl;
import edu.bupt.rpc.core.remote.dto.RpcRequest;
import edu.bupt.rpc.core.remote.transport.RpcRequestTransport;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 使用<b>socket</b>传输 <b>rpc</b>请求
 */
@Slf4j
public class SocketRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient(){
        this.serviceDiscovery = new ZkServiceDiscoveryImpl();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        try(Socket socket = new Socket()){
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        }catch (IOException | ClassNotFoundException e){
            throw new RuntimeException("请求RPC服务失败",e);
        }
    }
}
