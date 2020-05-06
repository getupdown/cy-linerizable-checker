package cn.cy.proxy.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * WriteBack2DownstreamHandler
 */
public class WriteBack2DownstreamHandler extends ChannelInboundHandlerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(WriteBack2DownstreamHandler.class);

    private final Channel writeBackChannel;

    public WriteBack2DownstreamHandler(Channel writeBackChannel) {
        this.writeBackChannel = writeBackChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("write back msg: {}", msg);
        writeBackChannel.writeAndFlush(msg);
    }
}
