import edu.bupt.rpc.core.annotation.RpcScan;
import edu.bupt.rpc.core.config.RpcServiceConfig;
import edu.bupt.rpc.core.remote.transport.netty.server.NettyRpcServer;
import hello.impl.HelloRpcImpl1;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RpcScan(basePackage = {"hello"})
public class NettyServer {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(NettyServer.class);
        NettyRpcServer nettyRpcServer = context.getBean("nettyRpcServer", NettyRpcServer.class);
        nettyRpcServer.start();
    }
}
