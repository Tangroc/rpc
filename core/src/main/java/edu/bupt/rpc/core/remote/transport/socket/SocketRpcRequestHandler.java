package edu.bupt.rpc.core.remote.transport.socket;

import edu.bupt.rpc.common.factory.SingletonFactory;
import edu.bupt.rpc.core.remote.dto.RpcRequest;
import edu.bupt.rpc.core.remote.dto.RpcResponse;
import edu.bupt.rpc.core.remote.handler.RpcRequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 基于socket连接，服务端处理请求的核心类
 */
@Slf4j
public class SocketRpcRequestHandler implements Runnable {
    private final Socket socket;
    private final RpcRequestHandler rpcRequestHandler;

    public SocketRpcRequestHandler(Socket socket) {
        this.socket = socket;
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    /**
     * 读取 rpc request
     * 处理
     * 返回结果
     */
    @Override
    public void run() {
        log.info("server[{}] handle rpc request from client[{}] by thread : [{}]", socket.getLocalAddress().getHostAddress(),
                ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress().getHostAddress(),Thread.currentThread().getName());
        try(ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())){
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Object result = rpcRequestHandler.handle(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.success(result,rpcRequest.getRequestId()));
            objectOutputStream.flush();
        }catch (IOException | ClassNotFoundException e){
            log.error("occur exception:",e);
        }
    }
}
