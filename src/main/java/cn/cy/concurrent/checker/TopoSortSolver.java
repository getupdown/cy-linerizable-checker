package cn.cy.concurrent.checker;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
     * 使用Set代替queue, 因为一个queue当中, 其实任何一个元素都可以被poll出来
     */
    public void enumerateAllTopoSort() {

        RecoverableLinkedList<Integer> queue = new RecoverableLinkedList<>();

        for (int i = 0; i < graphSize; i++) {
            if (degree[i] == 0) {
                queue.add(i);
            }
        }

        dfs(queue, Lists.newArrayList(), Sets.newHashSet());

    }

    /**
     * @param queue   队列状态
     * @param perm    排列
     * @param visited 以访问过的
     */
    @SuppressWarnings("unchecked")
    public void dfs(RecoverableLinkedList<Integer> queue, List<Integer> perm, Set<Integer> visited) {

        if (visited.size() == graphSize) {
            //finish
            System.out.println(perm);
            return;
        }

        // 随机从"队列"里取出一个元素
        for (RecoverableLinkedList.Node<Integer> e : queue) {
            List<Integer> targetNodes = lift(e, visited, queue, perm);

            dfs(queue, perm, visited);

            unlift(e, visited, queue, perm, targetNodes);
        }
    }

    /**
     * 从队列中取出节点x
     *
     * @param x
     *
     * @return 节点x连着的所有节点, 用于恢复现场
     */
    public List<Integer> lift(RecoverableLinkedList.Node<Integer> now, Set<Integer> visited,
                              RecoverableLinkedList<Integer> queue,
                              List<Integer> perm) {

        Integer x = now.getData();

        visited.add(x);
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
     * @param visited
     * @param queue
     * @param perm
     * @param targetNode
     */
    public void unlift(RecoverableLinkedList.Node<Integer> linkNode,
                       Set<Integer> visited,
                       RecoverableLinkedList<Integer> queue,
                       List<Integer> perm,
                       List<Integer> targetNode) {

        Integer x = linkNode.getData();

        visited.remove(x);

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
