package cn.cy.network;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.cy.network.echo.client.EchoClientWorker;

/**
 * echo client
 */
public class EchoClient {

    public static void main(String[] args) throws IOException, InterruptedException {

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20, 20, 0, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1024));

        for (int i = 0; i < 1; i++) {
            final long x = i;
            try {
                threadPoolExecutor.submit(new EchoClientWorker(x, "127.0.0.1", 8081));
            } catch (Exception e) {

            }
        }

        Thread.sleep(30000);

    }
}
