package cn.cy.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * 简单echo服务器
 */
public class SimpleEchoServer {

    public static void main(String[] args) throws IOException {

        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8081));

        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {

            selector.select();

            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            for (SelectionKey selectedKey : selectionKeys) {

                if (selectedKey.isAcceptable()) {
                    SocketChannel socketChannel = ((ServerSocketChannel) selectedKey.channel()).accept();
                    socketChannel.configureBlocking(false);
                    socketChannel
                            .register(selector, SelectionKey.OP_READ, new SocketDescriptor(socketChannel, selector));
                }

                if (selectedKey.isReadable()) {
                    SocketDescriptor socketDescriptor = (SocketDescriptor) selectedKey.attachment();
                    SocketChannel readableByteChannel = (SocketChannel) selectedKey.channel();
                    boolean consumeOver = socketDescriptor.consumeInput(readableByteChannel);
                    if (consumeOver) {
                        readableByteChannel.register(selector, SelectionKey.OP_WRITE);
                    }
                }

                if (selectionKey.isWritable()) {
                    SocketDescriptor socketDescriptor = (SocketDescriptor) selectedKey.attachment();
                }

                selectionKeys.remove(selectedKey);
            }
        }
    }
}
