package cn.cy.concurrent.mock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import cn.cy.concurrent.checker.SingleThreadImplChecker;
import cn.cy.concurrent.checker.TopoSortBasedChecker;

public class BrokenConcurrentCounterTest {

    private SingleThreadImplChecker singleThreadImplChecker = new CounterChecker();

    private volatile TopoSortBasedChecker checker;

    private volatile BrokenConcurrentCounter brokenConcurrentCounter;

    @Before
    public void setUp() throws Exception {
        this.checker = TopoSortBasedChecker.build(singleThreadImplChecker);

        brokenConcurrentCounter = new BrokenConcurrentCounter(this.checker);
    }

    @Test
    public void testA() throws InterruptedException {

        int threadN = 5;

        CountDownLatch countDownLatch = new CountDownLatch(threadN);

        Executor executor = Executors.newFixedThreadPool(threadN);

        for (int i = 0; i < threadN; i++) {
            executor.execute(() -> {
                brokenConcurrentCounter.add();
                brokenConcurrentCounter.get();
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();

        System.out.println(brokenConcurrentCounter.checkRes());
    }
}