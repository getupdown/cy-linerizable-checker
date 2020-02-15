package cn.cy.concurrent.lock;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import cn.cy.concurrent.lock.callback.TryingAcquireWatcher;
import cn.cy.concurrent.lock.zookeeper.ZkCli;

/**
 * 基于zookeeper的锁
 */
public class DistributedZkLock implements DistributeLock {

    public static final Logger logger = LoggerFactory.getLogger(DistributedZkLock.class);

    public static final String ZK_NODE_ROOT = "lock";

    private ZkCli zkCli;

    private ThreadLocal<String> nodeOnZkTree = new ThreadLocal<>();

    public DistributedZkLock(String ipList) throws IOException, InterruptedException {
        zkCli = new ZkCli(ipList, ZK_NODE_ROOT);
    }

    private String getLockPathPrefix() {
        return "/" + ZK_NODE_ROOT;
    }

    /**
     * 试图获取锁
     * <p>
     * 1. 首先, 在某个节点下, 创建{@link CreateMode#EPHEMERAL_SEQUENTIAL}节点
     * 1.1 如果这个节点是头节点, 那么说明获取到了锁
     * 1.2 如果不是头节点, 则进行等待操作
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
     * @return
     */
    private boolean acquire0() throws InterruptedException {

        // createNode : 首先创建节点
        String path = zkCli.createNode(getLockPathPrefix() + "/lock-", zkCli.getUuid(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);

        if (path == null) {
            logger.error("node create failed!");
            return false;
        }

        String node = path.substring(path.lastIndexOf("/") + 1);

        nodeOnZkTree.set(node);

        List<String> childrenList = Lists.newArrayList();

        // 自旋判断
        while (true) {
            // 查询子节点所在的下标
            Integer indexOfChildren = queryChildIndexWithSync(node, getLockPathPrefix(), 3, childrenList);

            Preconditions.checkNotNull(indexOfChildren);

            if (indexOfChildren == 0) {
                // 如果获取到锁了, 直接返回
                onAcquireLock(Thread.currentThread());
                return true;
            } else {
                // 如果没有获取到
                logger.debug("not acquired, begin to attach pred node");
                boolean attachRes = false;
                // 监听前驱节点
                // 如果前驱不存在了, 则重复查找, 直到查找到前驱节点或者自己成为第一个
                while (!attachRes) {
                    indexOfChildren = queryChildIndexWithSync(node, getLockPathPrefix(), 3, childrenList);

                    if (indexOfChildren == 0) {
                        onAcquireLock(Thread.currentThread());
                        return true;
                    }

                    String preNode = childrenList.get(indexOfChildren - 1);
                    attachRes = zkCli.attachWatcher(getLockPathPrefix() + "/" + preNode,
                            new TryingAcquireWatcher(Thread.currentThread()));
                }

                LockSupport.parkNanos(this, TimeUnit.SECONDS.toNanos(10));

                logger.info("thread is awaken!");
            }

        }
    }

    protected void onAcquireLock(Thread thread) {
        logger.debug("acquire lock!");
    }

    /**
     * 查询孩子节点
     *
     * @param targetNode
     * @param path
     * @param maxRetryTime
     * @param childrenListOnQueried
     */
    private Integer queryChildIndexWithSync(String targetNode, String path, Integer maxRetryTime,
                                            List<String> childrenListOnQueried)
            throws InterruptedException {

        Integer res = null;

        res = queryChildIndexWithRetry(targetNode, path, maxRetryTime, childrenListOnQueried);

        // 触发一次sync操作
        if (res == null) {
            logger.warn("trying find the index of the child : {} but not found after {} times", path, maxRetryTime);
            zkCli.syncBySync("/");
            res = queryChildIndexWithRetry(targetNode, path, maxRetryTime, childrenListOnQueried);
        }

        return res;
    }

    private Integer queryChildIndexWithRetry(String targetNode, String path, Integer maxRetryTime,
                                             List<String> childrenListOnQueried) {

        childrenListOnQueried.clear();

        for (Integer i = 0; i < maxRetryTime; i++) {

            List<String> children = zkCli.getChildren(path, null);

            logger.debug("path : {} , childrenList : {}", path, children);

            // 如果 createNode 和 getChildren打到同一台zk实例上, 根据%1, 一定有子节点
            Integer indexOfChildren = children.indexOf(targetNode);

            // 没有的话, 说明不是同一台
            if (indexOfChildren == -1) {
                logger.warn("indexOfChildren not found ! targetNode : {} , path : {}", targetNode, path);
                // 触发重试
                continue;
            }

            childrenListOnQueried.addAll(children);

            return indexOfChildren;
        }

        return null;
    }

    @Override
    public void acquire() throws InterruptedException {
        acquire0();
    }

    @Override
    public void acquire(long timeout, TimeUnit timeUnit) throws InterruptedException {

    }

    @Override
    public void release() {
        zkCli.delete(getLockPathPrefix() + "/" + nodeOnZkTree.get());
    }
}
