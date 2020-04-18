package cn.cy.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 简单echo服务器
 */
public class EchoServer {

    public static void main(String[] args) throws IOException {

        ExecutorService executorService = Executors.newFixedThreadPool(50);

        ServerSocket serverSocket = new ServerSocket();

        serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), 8080));

        while (true) {
            Socket socket = serverSocket.accept();

            System.out.println("received new connection! socket" + socket.toString());

            executorService.submit(() -> {
                int cnt = 0;
                while (true) {
                    try {
                        InputStream inputStream = socket.getInputStream();

                        byte[] bytes = new byte[4096];

                        int n = inputStream.read(bytes);

                        if (n > 0) {
                            // write back
                            cnt++;
                            if (cnt == 5) {
                                // server shutdown the write
                                System.out.println("server close the connection");
                                socket.close();
                                break;
                            }
                            System.out.println("write back!");
                            OutputStream outputStream = socket.getOutputStream();
                            outputStream.write(bytes);
                            outputStream.flush();
                        } else if (n == 0) {

                            // closed by
                            System.out.println("client closed!");
                            socket.close();

                        } else {

                            // error
                            System.out.println("client error!");

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }

            });
        }
    }

    private static class Worker {
        
    }
}
