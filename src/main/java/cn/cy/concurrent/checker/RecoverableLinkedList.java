package cn.cy.concurrent.checker;

import java.util.Iterator;

import com.google.common.base.Preconditions;

/**
 * 可恢复的双向链表(带头,尾节点)
 */
public class RecoverableLinkedList<T> implements Iterable<RecoverableLinkedList.Node> {

    /**
     * 节点类
     *
     * @param <T>
     */
    public static class Node<T> {
        private T data;
        private Node<T> prev;
        private Node<T> next;
        private boolean inList;

        public Node(T data, Node<T> prev, Node<T> next) {
            this.data = data;
            this.prev = prev;
            this.next = next;
            this.inList = true;
        }

        public T getData() {
            return data;
        }
    }

    /**
     * 头结点,不包含任何数据
     */
    private Node<T> head;

    /**
     * 尾节点,也不包含任何数据
     */
    private Node<T> tail;

    /**
     * 节点个数
     */
    private int size;

    public RecoverableLinkedList() {
        this.head = new Node<>(null, null, null);
        this.tail = new Node<>(null, null, null);

        this.head.next = tail;
        this.tail.prev = head;

        this.size = 0;
    }

    /**
     * 尾部插入节点
     *
     * @param data
     */
    public void add(T data) {
        Node node = new Node(data, tail.prev, tail);
        tail.prev.next = node;
        tail.prev = node;
        size++;
    }

    private Node<T> getHead() {
        return head;
    }

    private Node<T> getTail() {
        return tail;
    }

    /**
     * 移除当前节点,此节点可以通过recover函数恢复进入链表
     *
     * @param now
     *
     * @return
     */
    public Node<T> remove(Node<T> now) {

        Preconditions.checkNotNull(now);
        Preconditions.checkState(now.inList);

        Node prev = now.prev;

        if (prev != null) {
            prev.next = now.next;
        }

        Node next = now.next;
        if (next != null) {
            next.prev = now.prev;
        }

        size--;
        now.inList = false;

        return now;
    }

    /**
     * 恢复当前节点,进入链表
     *
     * @param now
     */
    public void recover(Node<T> now) {

        Preconditions.checkNotNull(now);
        Preconditions.checkState(!now.inList);

        Node tarPrev = now.prev;

        if (tarPrev != null) {
            tarPrev.next = now;
        }

        Node tarNext = now.next;
        if (tarNext != null) {
            tarNext.prev = now;
        }

        size++;
        now.inList = true;
    }

    /**
     * 从尾部开始删除n个节点
     *
     * @param cnt
     */
    public void removeFromTail(Integer cnt) {

        Node now = tail.prev;
        for (int i = 0; i < cnt; i++) {
            now = now.prev;
        }

        // help gc
        now.next = tail;
        tail.prev = now;
        size -= cnt;
    }

    @Override
    public Iterator<Node> iterator() {
        return new NodeIterator(this);
    }

    /**
     * 可恢复链表的节点迭代器
     */
    public static class NodeIterator implements Iterator<Node> {

        private RecoverableLinkedList bindList;

        private Node now;

        public NodeIterator(RecoverableLinkedList bindList) {
            Preconditions.checkNotNull(bindList);
            this.bindList = bindList;
            this.now = bindList.getHead();
        }

        @Override
        public boolean hasNext() {
            now = now.next;
            return now != bindList.getTail();
        }

        @Override
        public Node next() {
            Preconditions.checkNotNull(now);
            Preconditions.checkState(now.inList);
            return now;
        }
    }

    @Override
    public String toString() {
        String res = "";
        Node now = head.next;
        while (now != tail) {
            res = res + now.data + " ";
            now = now.next;
        }
        return res;
    }
}
