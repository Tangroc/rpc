package hello;

import edu.bupt.rpc.core.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RpcScan(basePackage = {"hello"})
public class NettyClient {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(NettyClient.class);
        HelloController helloController = context.getBean("helloController", HelloController.class);
        helloController.hello();
    }
}
