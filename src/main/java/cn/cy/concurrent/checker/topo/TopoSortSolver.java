package cn.cy.concurrent.checker.topo;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * 拓扑排序
 */
public class TopoSortSolver {

    private int graphSize;

    private int degree[];

    private boolean edge[][];

    public TopoSortSolver(int graphSize) {
        this.graphSize = graphSize;
        this.degree = new int[graphSize];
        this.edge = new boolean[graphSize][graphSize];
    }

    /**
     * 输出单个拓扑排序序列
     */
    public void topoSort() {

        List<Integer> res = Lists.newArrayList();

        Queue<Integer> queue = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < graphSize; i++) {
            if (degree[i] == 0) {
                queue.offer(i);
            }
        }

        while (!queue.isEmpty()) {
            Integer node = queue.poll();

            res.add(node);

            for (int i = 0; i < graphSize; i++) {
                if (edge[node][i]) {
                    degree[i]--;
                    if (degree[i] == 0) {
                        queue.offer(i);
                    }
                }
            }
        }
    }

    /**
     * 枚举所有拓扑排序的序列
     */
    public List<List<Integer>> enumerateAllTopoSort() {

        RecoverableLinkedList<Integer> queue = new RecoverableLinkedList<>();

        for (int i = 0; i < graphSize; i++) {
            if (degree[i] == 0) {
                queue.add(i);
            }
        }

        List<List<Integer>> allPerm = Lists.newArrayList();
        dfs(queue, Lists.newArrayList(), allPerm);

        return allPerm;
    }

    /**
     * @param queue 队列状态
     * @param perm  排列
     */
    @SuppressWarnings("unchecked")
    public void dfs(RecoverableLinkedList<Integer> queue, List<Integer> perm,
                    List<List<Integer>> allPerm) {

        if (queue.isEmpty()) {
            // finish
            allPerm.add(Lists.newArrayList(perm));
            return;
        }

        // 随机从"队列"里取出一个元素
        for (RecoverableLinkedList.Node<Integer> e : queue) {
            List<Integer> targetNodes = lift(e, queue, perm);

            dfs(queue, perm, allPerm);

            unlift(e, queue, perm, targetNodes);
        }
    }

    /**
     * 从队列中取出now节点
     *
     * @param now
     * @param queue
     * @param perm
     *
     * @return
     */
    public List<Integer> lift(RecoverableLinkedList.Node<Integer> now,
                              RecoverableLinkedList<Integer> queue,
                              List<Integer> perm) {

        Integer x = now.getData();

        perm.add(x);

        List<Integer> nodes = Lists.newArrayList();

        for (int i = 0; i < graphSize; i++) {
            if (edge[x][i]) {
                edge[x][i] = false;
                degree[i]--;
                if (degree[i] == 0) {
                    queue.add(i);
                }
                nodes.add(i);
            }
        }

        queue.remove(now);

        return nodes;
    }

    /**
     * 恢复现场
     *
     * @param linkNode
     * @param queue
     * @param perm
     * @param targetNode
     */
    public void unlift(RecoverableLinkedList.Node<Integer> linkNode,
                       RecoverableLinkedList<Integer> queue,
                       List<Integer> perm,
                       List<Integer> targetNode) {

        Integer x = linkNode.getData();

        Preconditions.checkState(perm.get(perm.size() - 1).equals(x));
        perm.remove(perm.size() - 1);

        Integer cnt = 0;

        for (Integer node : targetNode) {
            edge[x][node] = true;
            if (degree[node] == 0) {
                cnt++;
            }
            degree[node]++;
        }

        queue.recover(linkNode);

        queue.removeFromTail(cnt);
    }

    public void addEdge(int x, int y) {
        edge[x][y] = true;
        degree[y]++;
    }
}
