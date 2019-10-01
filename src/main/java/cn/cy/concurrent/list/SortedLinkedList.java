package cn.cy.concurrent.list;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 并发有序链表(集合)
 */
public class SortedLinkedList<V extends Comparable<V>> {

    private final Node<V> head;

    private final Node<V> tail;

    /**
     * 链表结点
     *
     * @param <V>
     */
    private static class Node<V extends Comparable<V>> {
        private volatile AtomicReference<Node<V>> next;
        private volatile V data;
        private volatile boolean inChain;
        private AtomicBoolean lockFlag;

        public Node(Node<V> next, V data) {
            this.next = new AtomicReference<>(next);
            this.data = data;
            this.inChain = true;
            this.lockFlag = new AtomicBoolean(false);
        }

        public void lock() {
            boolean suc = false;
            do {
                suc = lockFlag.compareAndSet(false, true);
            } while (!suc);
        }

        public void releaseLock() {
            boolean suc = false;
            do {
                suc = lockFlag.compareAndSet(true, false);
            } while (!suc);
        }
    }

    public SortedLinkedList() {
        // 初始化表尾节点
        tail = new Node<>(null, null);

        // 初始化表头结点
        head = new Node<>(tail, null);
    }

    /**
     * 插入数据
     *
     * @param data 数据体本身
     *
     * @return 数据是否插入, 若数据已有 返回false
     */
    public boolean insert(V data) {

        for (; ; ) {

            Node<V> now = head.next.get();
            Node<V> prev = head;

            boolean inserted = false;

            while (now != tail || now.data.compareTo(data) < 0) {
                prev = now;
                now = now.next.get();
            }

            try {
                now.lock();
                prev.lock();
                if (now.inChain && prev.inChain && prev.next.get() == now) {
                    // 取出前一个元素

                } else {
                    // 如果被删除了, 从头开始重找一遍
                    continue;
                }
            } finally {
                now.releaseLock();
                prev.releaseLock();
            }

            // prev next
            if (!inserted) {

            }
        }

    }

    /**
     * 检验是否包含数据
     *
     * @param data
     *
     * @return
     */
    public boolean contains(V data) {
        return false;
    }

    /**
     * 删除数据
     *
     * @param data
     *
     * @return 如果删除的数据不存在, 返回null
     */
    public V remove(V data) {
        return null;
    }
}
