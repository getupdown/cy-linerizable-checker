package cn.cy.concurrent.checker.runner;

import java.util.List;

import cn.cy.concurrent.checker.TopoSortBasedChecker;

public interface TestClass {

    /**
     * 获取checker
     *
     * @return
     */
    TopoSortBasedChecker getAttachedChecker();

    /**
     * 获取一个个测试
     *
     * @return
     */
    List<Runnable> getTestRunnable();
}
