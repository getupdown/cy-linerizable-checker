package cn.cy.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 *
 */
public class SocketDescriptor {

    private ByteBuffer byteBuffer;

    private StringBuilder stringBuilder;

    private String preCompleteMsg;

    private Boolean consumeFinish;

    private SocketChannel socketChannel;

    private Selector selector;

    public SocketDescriptor(SocketChannel socketChannel, Selector selector) {
        this.byteBuffer = ByteBuffer.allocateDirect(2048);
        this.stringBuilder = new StringBuilder();
        this.consumeFinish = false;
        this.socketChannel = socketChannel;
        this.selector = selector;
    }

    public boolean consumeInput(ReadableByteChannel readableByteChannel) throws IOException {

        boolean preFinish = false;

        int x = readableByteChannel.read(byteBuffer);
        System.out.println(x);
        byteBuffer.flip();

        while (byteBuffer.hasRemaining()) {
            char c = byteBuffer.getChar();
            if (c == '\n') {
                consumeFinish = true;
                break;
            }
            stringBuilder.append(c);
        }

        if (consumeFinish) {
            preCompleteMsg = stringBuilder.toString();
            stringBuilder = new StringBuilder();
            System.out.println(preCompleteMsg);
            preFinish = true;
        }

        consumeFinish = false;
        byteBuffer.clear();

        return preFinish;
    }

    public void writeBack() {

    }
}
