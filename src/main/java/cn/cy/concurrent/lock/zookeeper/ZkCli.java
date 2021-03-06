package cn.cy.concurrent.lock.zookeeper;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.cy.concurrent.lock.factory.ZooKeeperFactory;
import cn.cy.concurrent.lock.factory.ZooKeeperFactoryImpl;
import cn.cy.concurrent.lock.util.PathUtil;

public class ZkCli {

    public static final Logger logger = LoggerFactory.getLogger(ZkCli.class);

    private String ipList;

    private ZooKeeperFactory zooKeeperFactory;

    private String uuid;

    public static class CommonWatcher implements Watcher {

        private Semaphore startSemaphore;

        public CommonWatcher(Semaphore startSemaphore) {
            this.startSemaphore = startSemaphore;
        }

        @Override
        public void process(WatchedEvent event) {
            logger.info("event : {} happened!", JSON.toJSON(event));
            if (event.getState().equals(Event.KeeperState.SyncConnected)) {
                startSemaphore.release(1);
            }
        }
    }

    private String generateUUId() {
        return UUID.randomUUID().toString();
    }

    private ZkCli(String ipList) throws IOException, InterruptedException {
        this.ipList = ipList;
        this.zooKeeperFactory = new ZooKeeperFactoryImpl(ipList);
        uuid = generateUUId();
    }

    public ZkCli(String ipList, String ensureRootPath) throws IOException, InterruptedException {
        this(ipList);
        try {
            String res = zooKeeperFactory.getZooKeeperClientInstance(null)
                    .create("/" + ensureRootPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            logger.info("res : {}", res);
        } catch (KeeperException e) {
            if (e.code() == KeeperException.Code.NODEEXISTS) {
                logger.info(" node exists yet ! ");
            } else {
                logger.error("other error occurred ! ", e);
                throw wrapKeeperException(e);
            }
        }
    }

    public String createNode(String path, String data, List<ACL> aclList, CreateMode createMode) {
        try {
            return zooKeeperFactory.getZooKeeperClientInstance(null).create(path, data.getBytes(), aclList, createMode);
        } catch (KeeperException e) {
            logger.error("keeperException occurred ! path: {}, results: {}", e.getPath(), e.getResults(), e);
            throw wrapKeeperException(e);
        } catch (InterruptedException e) {
            logger.warn("create operation interrupted!");
            return null;
        }
    }

    public void removeNode(String path, int expectedVersion) {
        try {
            zooKeeperFactory.getZooKeeperClientInstance(null).delete(path, expectedVersion);
        } catch (InterruptedException e) {
            logger.warn("delete operation interrupted!");
        } catch (KeeperException e) {
            logger.error("keeperException occurred ! path: {}, results: {}", e.getPath(), e.getResults(), e);
            throw wrapKeeperException(e);
        }
    }

    public List<String> getChildren(String path, Watcher watcher) {
        try {
            return zooKeeperFactory.getZooKeeperClientInstance(null).getChildren(path, watcher);
        } catch (KeeperException e) {
            logger.error("keeperException occurred ! path: {}, results: {}", e.getPath(), e.getResults(), e);
            throw wrapKeeperException(e);
        } catch (InterruptedException e) {
            logger.warn("create operation interrupted!");
            return null;
        }
    }

    public boolean attachWatcher(String path, Watcher watcher) {
        try {
            Stat stat = zooKeeperFactory.getZooKeeperClientInstance(null).exists(path, watcher);
            logger.info("stat res is : {}", JSON.toJSONString(stat));
            return stat != null;
        } catch (KeeperException e) {
            logger.error("keeperException occurred ! path: {}, results: {}", e.getPath(), e.getResults(), e);
            throw wrapKeeperException(e);
        } catch (InterruptedException e) {
            logger.warn("attach watcher interrupted!");
            return false;
        }
    }

    public void delete(String node) {
        try {
            zooKeeperFactory.getZooKeeperClientInstance(null).delete(node, -1);
        } catch (InterruptedException e) {
            logger.warn("delete node : {} is interrupted", node);
        } catch (KeeperException e) {
            logger.error("keeperException occurred ! path: {}, results: {}", e.getPath(), e.getResults(), e);
            throw wrapKeeperException(e);
        }
    }

    /**
     * synchronous sync operation
     *
     * @param path
     */
    public void syncBySync(final String path) throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        zooKeeperFactory.getZooKeeperClientInstance(null).sync(path, new AsyncCallback.VoidCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx) {
                logger.info("sync operation return! result code is : {}", rc);
                countDownLatch.countDown();
            }
        }, null);

        logger.info("sync operation count down!");
        countDownLatch.await();
        logger.info("sync operation count down success!");
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
            List<String> children = zooKeeperFactory.getZooKeeperClientInstance(null).getChildren(rootPath, null);

            if (children.size() == 0) {
                return;
            }

            for (String child : children) {
                clearTree(PathUtil.concatPath(rootPath, child));
                zooKeeperFactory.getZooKeeperClientInstance(null).delete(PathUtil.concatPath(rootPath, child), -1);
            }

        } catch (KeeperException e) {
            logger.error("keeper Exception occurred on {}", rootPath);
            throw wrapKeeperException(e);
        } catch (InterruptedException e) {
        }
    }

    /**
     * 关闭客户端
     */
    public void close() throws InterruptedException {
        zooKeeperFactory.getZooKeeperClientInstance(null).close();
    }

    /**
     * zk异常包装
     *
     * @param keeperException
     *
     * @return
     */
    private ZKException wrapKeeperException(KeeperException keeperException) {
        return new ZKException(keeperException);
    }
}
