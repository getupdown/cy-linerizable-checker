package cn.cy.concurrent.mock;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import cn.cy.concurrent.checker.MethodCallDescriptor;
import cn.cy.concurrent.checker.SingleThreadImplChecker;

/**
 * 计数器检查
 */
public class CounterChecker implements SingleThreadImplChecker {

    private Integer cnt = 0;

    @Override
    public boolean check(List<MethodCallDescriptor> descriptorList) {

        if (CollectionUtils.isEmpty(descriptorList)) {
            return true;
        }

        for (MethodCallDescriptor methodCallDescriptor : descriptorList) {
            if (methodCallDescriptor.getMethodName().equals("add")) {
                cnt++;
            } else {
                Object ret = methodCallDescriptor.getRetVal();
                assert ret instanceof Integer;
                if (cnt != ret) {
                    return false;
                }
            }
        }

        return true;
    }
}
