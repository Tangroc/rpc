package hello.impl;

import edu.bupt.rpc.core.annotation.RpcService;
import hello.Hello;
import hello.HelloRpc;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RpcService(group = "s1",version = "1.0")
public class HelloRpcImpl1 implements HelloRpc {
    @Override
    public String hello(Hello hello) {
        log.info("HelloRpcImpl1 get message [{}]",hello.getMessage());
        String result = "Hi , this is HelloRpcImpl1 ";
        log.info("HelloRpcImpl1 return message [{}]",result);
        return result;
    }
}
