package cn.cy.concurrent.lock.zookeeper;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

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

    public static final Semaphore START_SEMAPHORE = new Semaphore(0);

    private ZooKeeper zooKeeper;

    private String uuid;

    public static class CommonWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            logger.info("event : {} happened!", JSON.toJSON(event));
            if (event.getState().equals(Event.KeeperState.SyncConnected)) {
                START_SEMAPHORE.release(1);
            }
        }
    }

    private String generateUUId() {
        return UUID.randomUUID().toString();
    }

    public ZkCli(String ipList) throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(ipList, 5000, new CommonWatcher());
        uuid = generateUUId();
        START_SEMAPHORE.acquire();
    }

    public String createNode(String path, String data, List<ACL> aclList, CreateMode createMode) {
        try {
            return zooKeeper.create(path, data.getBytes(), aclList, createMode);
        } catch (KeeperException e) {
            logger.error("keeperException occurred ! path: {}, results: {}", e.getPath(), e.getResults(), e);
            return null;
        } catch (InterruptedException e) {
            logger.warn("create operation interrupted!");
            return null;
        }
    }

    public List<String> getChildren(String path, Watcher watcher) {
        try {
            return zooKeeper.getChildren(path, watcher);
        } catch (KeeperException e) {
            logger.error("keeperException occurred ! path: {}, results: {}", e.getPath(), e.getResults(), e);
            return null;
        } catch (InterruptedException e) {
            logger.warn("create operation interrupted!");
            return null;
        }
    }

    public String getUuid() {
        return uuid;
    }
}
