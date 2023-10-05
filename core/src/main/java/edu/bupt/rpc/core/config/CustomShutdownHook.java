package edu.bupt.rpc.core.config;

import edu.bupt.rpc.common.utils.ThreadPoolFactoryUtil;
import edu.bupt.rpc.core.registry.zk.CuratorUtils;
import edu.bupt.rpc.core.remote.constant.RpcConstant;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    /**
     * 服务自动下线，注册中心清理
     */
    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), RpcConstant.NETTY_SERVER_PORT);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), address);
            } catch (UnknownHostException ignored) {
            }
            ThreadPoolFactoryUtil.shutDownAllThreadPool();
        }));
    }


}

