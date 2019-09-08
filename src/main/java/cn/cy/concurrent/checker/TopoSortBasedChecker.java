package cn.cy.concurrent.checker;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import cn.cy.concurrent.checker.topo.TopoSortSolver;

/**
 * 基于拓扑排序的checker
 */
public class TopoSortBasedChecker {

    private ConcurrentLinkedQueue<MethodCallDescriptor> mcdList = new ConcurrentLinkedQueue<>();

    private ThreadLocal<MethodCallDescriptor> localmcd = new ThreadLocal<>();

    private SingleThreadImplChecker singleThreadImplChecker;

    public TopoSortBasedChecker(SingleThreadImplChecker singleThreadImplChecker) {
        this.singleThreadImplChecker = singleThreadImplChecker;
    }

    /**
     * @param checker
     *
     * @return
     */
    public static TopoSortBasedChecker build(SingleThreadImplChecker checker) {
        return new TopoSortBasedChecker(checker);
    }

    /**
     * 开始记录方法的调用
     * ps : 方法不可嵌套使用
     *
     * @return
     */
    public void startRecord(String methodName, Object[] args) {
        MethodCallDescriptor mcd = new MethodCallDescriptor(methodName, Arrays.asList(args));
        mcd.setStartTime(LocalDateTime.now().toEpochSecond(ZoneOffset.MIN));

        localmcd.set(mcd);
    }

    public void endRecord(Object retVal) {
        MethodCallDescriptor mcd = localmcd.get();

        Preconditions.checkNotNull(mcd);

        mcd.setEndTime(LocalDateTime.now().toEpochSecond(ZoneOffset.MIN));
        mcd.setRetVal(retVal);

        mcdList.add(mcd);
    }

    /**
     * 校验执行序列是否满足"线性一致性"
     *
     * @return
     */
    public boolean check() {
        // 标号
        int x = 0;
        Map<Integer, MethodCallDescriptor> methodCallDescriptorMap = Maps.newHashMap();
        for (MethodCallDescriptor descriptor : mcdList) {
            methodCallDescriptorMap.put(x, descriptor);
            x++;
        }

        TopoSortSolver topoSortSolver = new TopoSortSolver(x);

        // 加边
        for (Map.Entry<Integer, MethodCallDescriptor> i : methodCallDescriptorMap
                .entrySet()) {

            for (Map.Entry<Integer, MethodCallDescriptor> j : methodCallDescriptorMap
                    .entrySet()) {

                if (i.getValue().getEndTime() < j.getValue().getStartTime()) {
                    topoSortSolver.addEdge(i.getKey(), j.getKey());
                }

            }
        }

        // 枚举全部的可能拓扑排序
        List<List<Integer>> res = topoSortSolver.enumerateAllTopoSort();

        // 对每个拓扑排序进行校验
        // 使用单线程执行序列进行校验
        for (List<Integer> eachPerm : res) {
            List<MethodCallDescriptor> descriptors =
                    eachPerm.stream().map(methodCallDescriptorMap::get).collect(
                            Collectors.toList());

            // 校验序列
            // 只要有序列可以符合,就认为这一次校验通过了
            if (singleThreadImplChecker.check(descriptors)) {
                return true;
            }
        }

        return false;
    }
}
