package cn.cy.sentinel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

/**
 * SentinelDemo
 */
public class SentinelDemo {

    public static void main(String[] args) throws InterruptedException {
        initAllRules();

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(new Worker("HelloWorld"));
        executorService.submit(new Worker("dependency"));

        TimeUnit.SECONDS.sleep(600);
    }

    public static class Worker implements Runnable {

        private String resourceName;

        public Worker(String resourceName) {
            this.resourceName = resourceName;
        }

        @Override
        public void run() {
            while (true) {
                Entry entry = null;
                try {
                    entry = SphU.entry(resourceName);
                    /*您的业务逻辑 - 开始*/
                    System.out.println(resourceName);
                    /*您的业务逻辑 - 结束*/
                } catch (BlockException e1) {
                    /*流控逻辑处理 - 开始*/
                    System.out.println(String.format("%s blocked", resourceName));
                    /*流控逻辑处理 - 结束*/
                } finally {
                    if (entry != null) {
                        entry.exit();
                    }
                }
            }
        }
    }

    private static void initAllRules() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule dependencyRule = new FlowRule();
        dependencyRule.setResource("dependency");
        dependencyRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        dependencyRule.setCount(100000);
        dependencyRule.setRefResource("HelloWorld");
        dependencyRule.setStrategy(RuleConstant.STRATEGY_RELATE);
        rules.add(dependencyRule);

        FlowRule helloWorldRule = new FlowRule();
        helloWorldRule.setResource("HelloWorld");
        helloWorldRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        helloWorldRule.setCount(70000);
        helloWorldRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        rules.add(helloWorldRule);

        FlowRuleManager.loadRules(rules);
    }
}
