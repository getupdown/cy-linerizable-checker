package cn.cy.concurrent.lock;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.cy.concurrent.debugger.MultiThreadDebugger;

/**
 * 本地clh锁,不可重入
 */
public class LocalCLHLock implements Lock {

    private static final Logger logger = LoggerFactory.getLogger(LocalCLHLock.class);

    private ThreadLocal<Node> threadLocal = new ThreadLocal<>();

    private final AtomicReference<Node> clhTail;

    private static class Node {
        private volatile Node pred;
        private volatile Node next;
        private volatile AtomicReference<NodeStatus> status = new AtomicReference<>(NodeStatus.INIT);
        private final Thread holdThread = Thread.currentThread();
        private final Long threadId = Thread.currentThread().getId();

        public Node(Node pred) {
            this.pred = pred;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        private enum NodeStatus {
            INIT,
            RELEASED,
            HOLDING,
            HOLDING_NEED_UNPARK,
            PENDING,
            PENDING_NEED_UNPARK
        }

        @Override
        public String toString() {
            return "Node {" + threadId + "}";
        }
    }

    public LocalCLHLock() {
        this.clhTail = new AtomicReference<>(null);
    }

    private Node getTail() {
        return clhTail.get();
    }

    private Node tryResolveFromThreadLocal() {
        Node trNode = threadLocal.get();
        if (!Objects.isNull(trNode)) {
            return trNode;
        } else {
            Node n = new Node(null);
            threadLocal.set(n);
            return n;
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

            Node preTail = getTail();
            now = tryResolveFromThreadLocal();

            // cas tail
            if (!clhTail.compareAndSet(preTail, now)) {
                continue;
            }

            now.pred = preTail;
            if (preTail != null) {
                preTail.next = now;
            }

            MultiThreadDebugger.log("add into queue, preTail is : {}", preTail);

            checkState(now.status.compareAndSet(Node.NodeStatus.INIT, Node.NodeStatus.PENDING));

            break;
        }

        // 自旋判断
        while (true) {
            Node pred = now.pred;
            MultiThreadDebugger.log("self spin , pred is {}, status : {}", pred);
            if (pred == null) {
                // 直接拿锁
                acquireLock0(now);
                MultiThreadDebugger.log("acquire lock :x0!");
                break;
            } else {
                // 查看前置状态
                if (pred.status.compareAndSet(Node.NodeStatus.HOLDING, Node.NodeStatus.HOLDING_NEED_UNPARK)
                        || pred.status.compareAndSet(Node.NodeStatus.PENDING, Node.NodeStatus.PENDING_NEED_UNPARK)) {
                    // 这时候就可以park了
                    // 得益于lockSupport的灵活性, 即: park和unpark顺序可以任意, 不会导致这里永远阻塞
                    // 所以直接调用就行了
                    MultiThreadDebugger.log("going to park status!");
                    LockSupport.park();

                    // 获取锁
                    acquireLock0(now);

                    MultiThreadDebugger.log("acquire lock :x1!");
                    break;
                } else if (pred.status.get().equals(Node.NodeStatus.RELEASED)) {
                    // 不再复用同一节点, 降低复杂度
                    // 获取锁
                    acquireLock0(now);

                    MultiThreadDebugger.log("acquire lock :x2!");
                    break;
                }
            }
        }
    }

    private void checkState(boolean res) {
        if (!res) {
            MultiThreadDebugger.error("should not happen!");
        }
    }

    private void acquireLock0(Node now) {
        // 获取锁
        now.pred = null;
        checkState(now.status.compareAndSet(Node.NodeStatus.PENDING, Node.NodeStatus.HOLDING)
                || now.status.compareAndSet(Node.NodeStatus.PENDING_NEED_UNPARK, Node.NodeStatus.HOLDING_NEED_UNPARK));
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
        Node n = tryResolveFromThreadLocal();

        if (n.status.compareAndSet(Node.NodeStatus.HOLDING, Node.NodeStatus.RELEASED)) {
            MultiThreadDebugger.log("lock released :y0! ");
        } else if (n.status.compareAndSet(Node.NodeStatus.HOLDING_NEED_UNPARK, Node.NodeStatus.RELEASED)) {
            LockSupport.unpark(n.next.holdThread);
            MultiThreadDebugger.log("lock released :y1! ");
        } else {
            MultiThreadDebugger.error("error branch!");
            throw new IllegalArgumentException("must be one branch!");
        }

        // release thread local
        threadLocal.set(null);
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
