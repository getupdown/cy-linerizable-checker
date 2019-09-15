package cn.cy.concurrent.lock;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 本地clh锁
 */
public class LocalCLHLock implements Lock {

    private ThreadLocal<Node> threadLocal = new ThreadLocal<>();

    private final AtomicReference<Node> clhTail;

    private static class Node {
        private volatile Node pred;
        private final Object syncObject = new Object();

        public Node(Node pred) {
            this.pred = pred;
        }
    }

    public LocalCLHLock() {
        this.clhTail = new AtomicReference<>(null);
    }

    private Node getTail() {
        return clhTail.get();
    }

    private Node tryGetFromThreadLocal(Node pred) {
        Node trNode = threadLocal.get();
        if (!Objects.isNull(trNode)) {
            trNode.pred = pred;
            return trNode;
        } else {
            return new Node(pred);
        }
    }

    /**
     * lock with block
     */
    @Override
    public void lock() {
        Node now = null;
        // 1. 增加节点进入链表
        for (; ; ) {
            Node tail = getTail();
            now = tryGetFromThreadLocal(tail);

            // cas tail
            if (!clhTail.compareAndSet(tail, now)) {
                continue;
            }
            threadLocal.set(now);
            break;
        }

        /*
        在《多处理器编程的艺术》上给的CLHLock算法, 是用一个循环空转, 来判断前置节点的状态
        这样做会让cpu空转, 如果等待队列很长的话, 会有大量的线程切换
        这里试图用线程阻塞的方式搞一下
         */
        // 2. 加入链表之后, 检查前置节点
        Node pred = now.pred;
        if (Objects.isNull(pred)) {
            // 如果前置是空, 说明他目前是队列首部, 拿到锁了
        } else {
            // 检查前置的状态
            
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
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
