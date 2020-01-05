package cn.cy.concurrent.lock;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.cy.concurrent.lock.zookeeper.ZkCli;

/**
 * 基于zookeeper的锁
 */
public class DistributedZkLock implements Lock {

    public static final Logger logger = LoggerFactory.getLogger(DistributedZkLock.class);

    public static final String ZK_NODE_ROOT = "/lock";

    private ZkSyncObject zkSyncObject;

    /**
     * 基于AQS实现锁对象
     */
    private static class ZkSyncObject extends AbstractQueuedSynchronizer {

        private ZkCli zkCli;

        /**
         * 每个线程自己维护一个snapshotVersion
         */
        private ThreadLocal<Integer> snapShotVersion;

        private ThreadLocal<ReentrantStatus> reentrantStatusThreadLocal;

        private enum ReentrantStatus {
            INIT,
            TRY_ACQUIRING,
            ACQUIRED
        }

        /**
         * 试图获取信号量
         * <p>
         * 1. 首先, 在某个节点下, 创建{@link CreateMode#EPHEMERAL_SEQUENTIAL}节点
         * 1.1 如果这个节点是头节点, 那么说明获取到了锁
         * 1.2 如果不是头节点, 由aqs处理node对象进入本地队列, 等待唤醒
         * <p>
         * 写这个方法时, 基于以下几个事实
         * <p>
         * %1.
         * 假设create请求打到了follower上, 并且正常返回
         * 然后立刻getChildren,产生了树的状态S
         * 那么, S一定包含之前请求成功的create信息, 即新创建的节点一定在其中
         * <p>
         * 原因:
         * 查看了follower处理请求的源码, follower是由以下3个职责链处理器组成的
         * FollowerRequestProcessor -> CommitProcessor -> FinalRequestProcessor
         * <p>
         * FollowerRequestProcessor会把所有"transaction"请求, 异步转发到leader上去
         * CommitProcessor会等待在FollowerRequestProcessor提交的那个请求, 收到commit响应(即经过一致性保证机制,这个数据可以写上去),
         * 才会交给FinalRequestProcessor, follower会把数据插入到自己本地的备份中去
         * 所以后面再getChildren, 一定就可以拿到了
         * <p>
         * %2:
         * case详见 {@link cn.cy.concurrent.lock.zookeeper.ZkCliTest#attachWatcherToNonExist()}
         *
         * @param arg
         *
         * @return
         */
        @Override
        protected boolean tryAcquire(int arg) {

            if (!lazySetAndCheckReentrantStatus()) {
                throw new IllegalArgumentException("the thread is now trying acquire!");
            }

            String thisPath = zkCli.createNode("/", zkCli.getUuid(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);

            logger.info("acquired into queue, thisPath:{}", thisPath);

            if (thisPath == null) {
                logger.warn("create path : {} failed ! ", zkCli.getUuid());
                return false;
            }

            onTryAcquiring();

            // 获取父节点的所有子节点
            List<String> children = zkCli.getChildren("/", null);

            // 异常, 当做没有抢到
            if (children == null) {
                logger.warn("uuid : {}, getChildren method error ! ", zkCli.getUuid());
                return false;
            }

            // 查看自己所处位置
            Integer thisPathIndex = children.indexOf(thisPath);

            if (logger.isDebugEnabled()) {
                logger.debug("childeren list : {}", children);
            }

            // %1 理论上不会出现这种情况
            if (thisPathIndex == -1) {
                logger.error("thisPath not occurred in children list ! {}", thisPath);
                return false;
            }

            // 如果是第一个, 就直接拿到锁了
            if (thisPathIndex == 0) {
                logger.debug("acquired the lock! uuid : {}", zkCli.getUuid());
                onTryAcquireSuccess();
                return true;
            }

            Integer attachTarget = thisPathIndex - 1;
            while (true) {
                // %2 虽然实际应用中不会只连单点, 但还是有可能会抛出connectionLoss异常, 考虑异常情况
                boolean exist = zkCli.attachWatcher(children.get(attachTarget), watchedEvent -> {

                });

                // 如果这个元素不存在了
                if (!exist) {
                    // 第0个元素还不存在, 就是获取到了
                    if (attachTarget == 0) {
                        onTryAcquireSuccess();
                        logger.debug("acquired the lock on multi trying! uuid : {}", zkCli.getUuid());
                        return true;
                    } else {
                        attachTarget--;
                    }
                } else {
                    return false;
                }
            }
        }

        private void onTryAcquireSuccess() {
            reentrantStatusThreadLocal.set(ReentrantStatus.ACQUIRED);
        }

        private void onTryAcquiring() {
            reentrantStatusThreadLocal.set(ReentrantStatus.TRY_ACQUIRING);
        }

        @Override
        protected boolean tryRelease(int arg) {
            return super.tryRelease(arg);
        }

        /**
         *
         */
        private boolean lazySetAndCheckReentrantStatus() {
            ReentrantStatus reentrantStatus = reentrantStatusThreadLocal.get();

            if (reentrantStatus == null) {
                reentrantStatusThreadLocal.set(ReentrantStatus.INIT);
                reentrantStatus = ReentrantStatus.INIT;
            }

            return reentrantStatus.equals(ReentrantStatus.INIT);
        }
    }

    @Override
    public void lock() {
        zkSyncObject.acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        zkSyncObject.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return zkSyncObject.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {

    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
