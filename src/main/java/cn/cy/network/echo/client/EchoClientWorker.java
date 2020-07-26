package cn.cy.network.echo.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.collect.Lists;

/**
 * EchoClientWorker
 */
public class EchoClientWorker implements Runnable {

    private Long workerId;

    private SocketChannel socketChannel;

    public EchoClientWorker(Long workerId, String ip, Integer port) throws IOException {
        this.workerId = workerId;
        socketChannel = SocketChannel.open(new InetSocketAddress(InetAddress.getByName(ip), port));
    }

    @Override
    public void run() {

        OUT:
        while (true) {
            int base = 0;
            int len = base + ThreadLocalRandom.current().nextInt(0, 40);

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < len; i++) {
                sb.append('a');
            }

            String input = sb.toString() + "\n";
            System.out.println(String.format("length is %d", len));
            System.out.println(String.format("content is %s", input));

            Integer batchSize = 1024;

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(batchSize);

            byte[] byteArray = input.getBytes(StandardCharsets.UTF_8);

            int nowPt = 0;
            int writeCnt = 0;

            while (nowPt < byteArray.length) {

                int next = Math.min(nowPt + batchSize, byteArray.length);

                // 首先使用buffer的输入模式进行输入, 并且flip成输出模式
                byteBuffer.put(byteArray, nowPt, next - nowPt).flip();

                int thisWriteCnt = 0;

                // buffer输出到channel里
                try {
                    thisWriteCnt = socketChannel.write(byteBuffer);
                } catch (IOException e) {
                    System.out.println("error occurred " + e);
                    break OUT;
                }
                System.out.println("write cnt " + thisWriteCnt);
                if (thisWriteCnt == 0) {
                    System.out.println("write over?");
                    byteBuffer.clear();
                    break;
                }

                writeCnt += thisWriteCnt;
                byteBuffer.clear();
                nowPt = next;
            }

            boolean readOver = false;

            List<Byte> bytes = Lists.newArrayList();

            int readCnt = 0;
            while (!readOver) {

                int thisReadCnt = 0;

                // buffer的输入模式, buffer从channel输入
                try {
                    thisReadCnt = socketChannel.read(byteBuffer);
                } catch (IOException e) {
                    System.out.println("error ocurred when reading " + e);
                    break;
                }

                System.out.println("read cnt " + thisReadCnt);

                if (thisReadCnt == 0) {
                    readOver = true;
                    System.out.println("read return 0");
                    break OUT;
                }

                // flip成输出模式
                byteBuffer.flip();
                readCnt += thisReadCnt;

                // buffer输出
                for (int i = 0; i < thisReadCnt; i++) {
                    byte b = byteBuffer.get();
                    if (b == '\n') {
                        readOver = true;
                        break;
                    }
                    bytes.add(b);
                }

                byteBuffer.clear();
            }

        }

    }
}
