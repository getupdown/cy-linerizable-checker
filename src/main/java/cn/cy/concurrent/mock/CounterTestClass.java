package cn.cy.concurrent.mock;

import java.util.List;

import com.google.common.collect.Lists;

import cn.cy.concurrent.checker.TopoSortBasedChecker;
import cn.cy.concurrent.checker.runner.TestClass;

/**
 *
 */
public class CounterTestClass implements TestClass {

    private TopoSortBasedChecker topoSortBasedChecker;

    private List<Runnable> registeredRunnable;

    private CounterTestClass(TopoSortBasedChecker topoSortBasedChecker) {
        this.topoSortBasedChecker = topoSortBasedChecker;
        this.registeredRunnable = Lists.newArrayList();
    }

    public static CounterTestClass bindWithChecker(TopoSortBasedChecker checker) {
        return new CounterTestClass(checker);
    }

    public CounterTestClass register(Runnable runnable) {
        this.registeredRunnable.add(runnable);
        return this;
    }

    @Override
    public TopoSortBasedChecker getAttachedChecker() {
        return topoSortBasedChecker;
    }

    @Override
    public List<Runnable> getTestRunnable() {
        return null;
    }
}
