package edu.bupt.rpc.core.registry.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CuratorUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() throws Exception {
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> list = zkClient.getChildren().forPath("/");
        System.out.println(list);
    }
}