package cn.cy.concurrent.lock.zookeeper;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class ZkCli {

    public static final Logger logger = LoggerFactory.getLogger(ZkCli.class);

    private ZooKeeper zooKeeper;

    public static class DebugWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            logger.info("event : {} happened!", JSON.toJSON(event));
        }
    }

    public ZkCli(String ipList) throws IOException {
        zooKeeper = new ZooKeeper(ipList, 5000, new DebugWatcher());
    }

    public String createNode(String path, String data, List<ACL> aclList, CreateMode createMode) {
        try {
            return zooKeeper.create(path, data.getBytes(), aclList, createMode);
        } catch (KeeperException e) {
            logger.error("keeperException occurred ! path: {}, results: {}", e, e.getPath(), e.getResults());
            return null;
        } catch (InterruptedException e) {
            logger.warn("create operation interrupted!");
            return null;
        }
    }

}
