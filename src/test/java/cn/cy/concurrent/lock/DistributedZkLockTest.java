package cn.cy.concurrent.lock;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DistributedZkLockTest {

    public static final Logger logger = LoggerFactory.getLogger(DistributedZkLock.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    /**
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void acquire() throws IOException, InterruptedException {

        DistributedZkLock distributedZkLock = new DistributedZkLock("118.31.4.42:2182" + ",118.31.4"
                + ".42:2181");

        executorService.submit(() -> {
            try {
                distributedZkLock.acquire();
                logger.info("1 acquired!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        System.out.println("xxxx");

        executorService.submit(() -> {
            try {
                distributedZkLock.acquire();
                logger.info("2 acquired!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread.sleep(120000);
    }
}
