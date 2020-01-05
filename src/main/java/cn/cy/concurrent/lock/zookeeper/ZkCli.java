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
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.cy.concurrent.util.PathUtil;

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
        zooKeeper = new ZooKeeper(ipList, 600000, new CommonWatcher());
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

    public void removeNode(String path, int expectedVersion) {
        try {
            zooKeeper.delete(path, expectedVersion);
        } catch (InterruptedException e) {
            logger.warn("delete operation interrupted!");
        } catch (KeeperException e) {
            logger.error("keeperException occurred ! path: {}, results: {}", e.getPath(), e.getResults(), e);
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

    public boolean attachWatcher(String path, Watcher watcher) {
        try {
            Stat stat = zooKeeper.exists(path, watcher);
            return stat != null;
        } catch (KeeperException e) {
            logger.error("keeperException occurred ! path: {}, results: {}", e.getPath(), e.getResults(), e);
            return false;
        } catch (InterruptedException e) {
            logger.warn("attach watcher interrupted!");
            return false;
        }
    }

    public String getUuid() {
        return uuid;
    }

    /**
     * for debug
     * <p>
     * begin
     */
    public void clearTree(String rootPath) {
        try {
            List<String> children = zooKeeper.getChildren(rootPath, null);

            if (children.size() == 0) {
                return;
            }

            for (String child : children) {
                clearTree(PathUtil.concatPath(rootPath, child));
                zooKeeper.delete(PathUtil.concatPath(rootPath, child), -1);
            }

        } catch (KeeperException e) {
            logger.error("keeper Exception occurred on {}", rootPath);
        } catch (InterruptedException e) {
        }
    }
}
