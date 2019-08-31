package cn.cy.concurrent.checker;

import java.util.List;

/**
 * 把序列使用单线程实现对象进行校验
 */
public interface SingleThreadImplChecker {

    /**
     * 给定一个序列, 校验他在单线程运行下是否成立
     *
     * @param descriptorList
     *
     * @return
     */
    boolean check(List<MethodCallDescriptor> descriptorList);
}
