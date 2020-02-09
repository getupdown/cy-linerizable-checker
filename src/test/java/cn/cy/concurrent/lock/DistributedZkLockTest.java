package cn.cy.concurrent.lock;

import static cn.cy.concurrent.lock.zookeeper.ZkCliTest.BASE_PATH;

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

    @Test
    public void acquire() throws IOException, InterruptedException {

        DistributedZkLock distributedZkLock = new DistributedZkLock("118.31.4.42:2182" + BASE_PATH);

        executorService.submit(() -> {
            try {
                distributedZkLock.acquire();
                logger.info("1 acquired!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        executorService.submit(() -> {
            try {
                distributedZkLock.acquire();
                logger.info("2 acquired!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread.sleep(60000);
    }
}
