package cn.cy.concurrent.entrance;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.cy.concurrent.lock.DistributedZkLock;

/**
 *
 */
public class DistributedLockEntrance {

    public static final Logger LOGGER = LoggerFactory.getLogger(DistributedLockEntrance.class);

    public static void main(String[] args) throws IOException, InterruptedException {

        int n = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(n);

        for (int i = 0; i < n; i++) {

            executorService.submit(() -> {
                DistributedZkLock distributedZkLock = null;
                try {
                    distributedZkLock = new DistributedZkLock("118.31.4.42:2182,118.31.4.42:2183");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < 1000; j++) {
                    try {
                        distributedZkLock.acquire();

                    } catch (InterruptedException e) {
                        // acquire
                    } finally {
                        distributedZkLock.release();
                    }
                }
            });
        }

    }

}
