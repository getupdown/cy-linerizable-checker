package cn.cy.concurrent.checker;

import java.util.List;

public class MethodCallDescriptor {

    private String methodName;

    private List<Object> inputArgs;

    private Object retVal;

    private Long startTime;

    private Long endTime;

    public MethodCallDescriptor(String methodName, List<Object> inputArgs) {
        this.methodName = methodName;
        this.inputArgs = inputArgs;
    }

    public void setInputArgs(List<Object> inputArgs) {
        this.inputArgs = inputArgs;
    }

    public void setRetVal(Object retVal) {
        this.retVal = retVal;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }
}
