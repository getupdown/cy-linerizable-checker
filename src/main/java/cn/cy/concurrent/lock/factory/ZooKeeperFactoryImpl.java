package cn.cy.concurrent.lock.factory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.function.Consumer;

import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.cy.concurrent.lock.zookeeper.ZkCli;

/**
 * zooKeeperFactory
 * <p>
 * 一个ZKLock实例绑定一个Factory
 */
public class ZooKeeperFactoryImpl implements ZooKeeperFactory {

    public static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperFactoryImpl.class);

    private final String ipList;

    private final AtomicStampedReference<FutureTask<ZooKeeper>> zooKeeperFutureTask;

    public ZooKeeperFactoryImpl(String ipList) {
        this.ipList = ipList;
        this.zooKeeperFutureTask = new AtomicStampedReference<>(null, 0);
    }

    /**
     * 可能被多线程调用
     *
     * @param onCreateNewCallback
     *
     * @return
     */
    @Override
    public ZooKeeper getZooKeeperClientInstance(Consumer<ZooKeeper> onCreateNewCallback) {
        while (true) {
            ZooKeeper zooKeeper = null;
            int[] holder = new int[1];
            FutureTask<ZooKeeper> futureTask = zooKeeperFutureTask.get(holder);
            try {
                if (futureTask != null) {
                    zooKeeper = futureTask.get();
                }
            } catch (InterruptedException e) {
                LOGGER.error("getZooKeeperClientInstance interrupted!");
                // ignore the result
                return null;
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

            if (zooKeeper == null || !zooKeeper.getState().isConnected()) {
                FutureTask<ZooKeeper> newFuture =
                        new FutureTask<>(new ConstructCallableImpl(ipList, onCreateNewCallback));
                if (zooKeeperFutureTask.compareAndSet(futureTask, newFuture, holder[0], holder[0] + 1)) {
                    newFuture.run();
                }
            } else {
                return zooKeeper;
            }
        }
    }

    private static class ConstructCallableImpl implements Callable<ZooKeeper> {

        private final String ipList;

        private final Consumer<ZooKeeper> onCreateNewCallback;

        public ConstructCallableImpl(String ipList,
                                     Consumer<ZooKeeper> onCreateNewCallback) {
            this.ipList = ipList;
            this.onCreateNewCallback = onCreateNewCallback;
        }

        @Override
        public ZooKeeper call() throws Exception {

            Semaphore semaphore = new Semaphore(0);

            ZooKeeper zooKeeper = new ZooKeeper(ipList, 6000, new ZkCli.CommonWatcher(semaphore));

            semaphore.acquire();

            if (onCreateNewCallback != null) {
                onCreateNewCallback.accept(zooKeeper);
            }

            return zooKeeper;
        }
    }
}
