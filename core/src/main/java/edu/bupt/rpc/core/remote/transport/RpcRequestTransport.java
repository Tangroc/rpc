package edu.bupt.rpc.core.remote.transport;

import edu.bupt.rpc.core.remote.dto.RpcRequest;

public interface RpcRequestTransport {
    /**
     * 发送rpc请求，接收结果
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
