package cn.cy.concurrent.executor;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * MonitoredThreadPoolExecutorTest
 */
public class MonitoredThreadPoolExecutorTest {

    @Test
    public void name() throws InterruptedException {
        MonitoredThreadPoolExecutor monitoredThreadPoolExecutor = new MonitoredThreadPoolExecutor(1, 50, 1,
                TimeUnit.SECONDS, new SynchronousQueue<>(), Thread::new,
                new ThreadPoolExecutor.CallerRunsPolicy());

        for (int i = 0; i < 1000; i++) {
            monitoredThreadPoolExecutor.submit(new WorkerTask());
        }

        TimeUnit.SECONDS.sleep(100);
    }

    @Test
    public void test() {

        System.out.println((-1) << 31);

    }

    static final class WorkerTask implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                int x = 3;
            }
        }
    }
}
