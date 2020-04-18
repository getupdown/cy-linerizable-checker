package cn.cy.concurrent.lock.factory;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 *
 */
public class ZooKeeperFactoryImplTest {

    public static final Logger logger = LoggerFactory.getLogger(ZooKeeperFactoryImplTest.class);

    @Test
    public void getZooKeeperClientInstance() throws KeeperException, InterruptedException {

        ZooKeeperFactory zooKeeperFactory = new ZooKeeperFactoryImpl("118.31.4.42:2182");

        Object lockObj = new Object();

        HashSet<ZooKeeper> zooKeepers = Sets.newHashSet();

        Executors.newFixedThreadPool(1).submit(new ZooKeeperCliSweeper(lockObj, zooKeepers));

        int zkCliThreadCnt = 10;
        ExecutorService zkCliExecutor = Executors.newFixedThreadPool(zkCliThreadCnt);

        for (int i = 0; i < zkCliThreadCnt; i++) {
            zkCliExecutor.submit(() -> {
                while (true) {

                    ZooKeeper zooKeeper = zooKeeperFactory.getZooKeeperClientInstance(
                            zk -> {
                                synchronized(lockObj) {
                                    Long sessionId = zk.getSessionId();
                                    logger.info("zk session:{} created!", sessionId);
                                    zooKeepers.add(zk);
                                }
                            }
                    );

                    try {
                        logger.info("session : {} getting data !", zooKeeper.getSessionId());
                        zooKeeper.getData("/", null, new Stat());
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Thread.sleep(3000 + new Random().nextInt(2000));
                }
            });
        }

        Thread.sleep(600000);

    }

    private static class ZooKeeperCliSweeper implements Runnable {

        public static final Logger logger = LoggerFactory.getLogger(ZooKeeperCliSweeper.class);

        public final Object lockObject;

        private final HashSet<ZooKeeper> allAliveZks;

        public ZooKeeperCliSweeper(Object lockObject, HashSet<ZooKeeper> allAliveZks) {
            this.lockObject = lockObject;
            this.allAliveZks = allAliveZks;
        }

        @Override
        public void run() {
            while (true) {
                synchronized(lockObject) {
                    int size = allAliveZks.size();
                    logger.info("detect {} active Zk", size);
                    // 不能同时有2个有效的zk对象
                    Assert.assertTrue(size == 0 || size == 1);

                    // 关闭zk对象
                    allAliveZks.iterator().forEachRemaining(zooKeeper -> {
                        Long sessionId = zooKeeper.getSessionId();
                        try {
                            zooKeeper.close();
                            logger.info("zk session:{} closed", sessionId);
                        } catch (InterruptedException e) {
                            logger.error("zk session:{} closed interrupted!", sessionId);
                        }
                    });

                    allAliveZks.clear();
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    logger.error("sweeper interrupted!", e);
                }
            }

        }
    }
}
