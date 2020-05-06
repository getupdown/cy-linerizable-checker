package cn.cy.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * echo client
 */
public class EchoClient {

    public static void main(String[] args) throws IOException {

        System.out.println();

        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8080));

        while (true) {

            Scanner scanner = new Scanner(System.in);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);

            while (scanner.hasNext()) {
                String x = scanner.next();
                byteBuffer = byteBuffer.put(x.getBytes());

                byteBuffer.flip();

                socketChannel.write(byteBuffer);

                socketChannel.read(byteBuffer);

                byteBuffer.clear();

                Boolean hasReadEof = false;
            }
        }
    }

    public void achrive() throws IOException {
        Socket socket = new Socket();
        socket.setTcpNoDelay(true);
        socket.bind(null);
        socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), 8080));

        while (true) {
            Scanner scanner = new Scanner(System.in);

            while (scanner.hasNext()) {
                String x = scanner.next();

                try {
                    socket.getOutputStream().write(x.getBytes());

                    byte[] bytes = new byte[4096];
                    System.out.println("init!");

                    int n = socket.getInputStream().read(bytes);

                    System.out.println("read " + n);

                    if (n > 0) {
                        System.out.println(new String(bytes));
                    } else if (n == 0) {
                        System.out.println("client received 0!");
                    } else {
                        System.out.println("client received error!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
