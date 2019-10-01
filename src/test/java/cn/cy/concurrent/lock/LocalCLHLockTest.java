package cn.cy.concurrent.lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import cn.cy.concurrent.debugger.MultiThreadDebugger;

/**
 *
 */
public class LocalCLHLockTest {

    private final LocalCLHLock lock = new LocalCLHLock();

    @Test
    public void name() throws InterruptedException {

        int tn = 10;

        ExecutorService e = Executors.newFixedThreadPool(tn);

        CountDownLatch countDownLatch = new CountDownLatch(tn);

        for (int i = 0; i < tn; i++) {
            e.submit(() -> {
                lock.lock();
                MultiThreadDebugger.log("抢到锁了!");

                testFunction();

                lock.unlock();
                MultiThreadDebugger.log("释放锁了!");

                lock.lock();
                MultiThreadDebugger.log("抢到锁了!");

                testFunction();

                lock.unlock();
                MultiThreadDebugger.log("释放锁了!");

                countDownLatch.countDown();
            });
        }

        countDownLatch.await();
    }

    private void testFunction() {
        for (int i = 0; i < 10; i++) {
            MultiThreadDebugger.log("i'm working ! ");
        }
    }
}