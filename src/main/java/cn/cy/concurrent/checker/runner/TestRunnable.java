package cn.cy.concurrent.checker.runner;

import java.util.List;

import cn.cy.concurrent.checker.MethodCallDescriptor;

/**
 *
 */
public interface TestRunnable extends Runnable {

    List<MethodCallDescriptor> runWithDescriptorRecorded();

}
