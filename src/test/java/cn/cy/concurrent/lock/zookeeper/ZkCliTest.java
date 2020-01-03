package cn.cy.concurrent.lock.zookeeper;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.junit.Test;

public class ZkCliTest {

    private ZkCli zkCli;

    @Test
    public void testAccess() throws IOException, InterruptedException {

        zkCli = new ZkCli("118.31.4.42:2181");

        zkCli.createNode("/test1", "data1", ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }
}