package cn.cy.concurrent;

import java.util.Objects;

/**
 * 并发有序链表
 */
public class SortedLinkedList<V extends Comparable<V>> {

    private Node head;

    /**
     * 链表结点
     *
     * @param <V>
     */
    private static class Node<V extends Comparable<V>> {
        private volatile Node next;
        private volatile V data;
        private volatile boolean marked;

        public Node(Node next, V data) {
            this.next = next;
            this.data = data;
            this.marked = false;
        }

        public void mark() {
            this.marked = true;
        }
    }

    public SortedLinkedList() {
        // 初始化表头结点
        head = new Node(null, null);
    }

    /**
     * 插入数据
     *
     * @param data
     *
     * @return
     */
    public V insert(V data) {
        Node now = head.next;
        Node pre = head;
        while (true) {
            if (Objects.isNull(now)) {
                now = new Node(null, data);
                pre.next = now;
                break;
            }

            if (now.data.compareTo(data) < 0) {
                pre = now;
                now = now.next;
            } else if (now.data.compareTo(data) == 0) {

                Node n = new Node(now.next, data);
                pre.next = n;

                break;
            }
        }
        return data;
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
