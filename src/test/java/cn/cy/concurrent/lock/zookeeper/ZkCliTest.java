package cn.cy.concurrent.lock.zookeeper;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.junit.Test;

public class ZkCliTest {

    private ZkCli zkCli;

    @Test
    public void createNode() throws IOException {

        zkCli = new ZkCli("118.31.4.42:2181,118.31.4.42:2182,118.31.4.42:2183");

        String res = zkCli.createNode("/test", "test", null, CreateMode.EPHEMERAL);

        System.out.println(res);

    }
}