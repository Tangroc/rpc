package hello;

import edu.bupt.rpc.core.annotation.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class HelloController {
    @RpcReference(group = "s1",version = "1.0")
    private HelloRpc helloRpc;

    public void hello(){
        String result = helloRpc.hello(new Hello("client hello", "client send hello message to s1"));
        System.out.println(result);
    }
}
