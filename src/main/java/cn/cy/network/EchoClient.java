package cn.cy.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Scanner;

import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;

/**
 * echo client
 */
public class EchoClient {

    public static void main(String[] args) throws IOException {

        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8081));

        while (true) {

            Scanner scanner = new Scanner(System.in);

            Integer batchSize = 1024;

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(batchSize);

            while (scanner.hasNext()) {
                String x = scanner.next() + "\n";

                byte[] byteArray = x.getBytes();

                int nowPt = 0;

                while (nowPt < byteArray.length) {

                    int next = Math.min(nowPt + batchSize, byteArray.length);

                    // 首先使用buffer的输入模式进行输入, 并且flip成输出模式
                    byteBuffer.put(byteArray, nowPt, next - nowPt).flip();

                    // buffer输出到channel里
                    int writeCnt = socketChannel.write(byteBuffer);
                    if (writeCnt == 0) {
                        System.out.println("write over?");
                        byteBuffer.clear();
                        break;
                    }
                    byteBuffer.clear();
                    nowPt = next;
                }

                boolean readOver = false;

                List<Byte> bytes = Lists.newArrayList();

                while (!readOver) {

                    // buffer的输入模式, buffer从channel输入
                    int readCnt = socketChannel.read(byteBuffer);

                    System.out.println("read cnt " + readCnt);

                    if (readCnt == 0) {
                        readOver = true;
                        System.out.println("read return 0");
                        break;
                    }

                    // flip成输出模式
                    byteBuffer.flip();

                    // buffer输出
                    for (int i = 0; i < readCnt; i++) {
                        byte b = byteBuffer.get();
                        if (b == '\n') {
                            readOver = true;
                            break;
                        }
                        bytes.add(b);
                    }

                    byteBuffer.clear();
                }

                System.out.println(new String(Bytes.toArray(bytes)));
            }
        }
    }
}
