package cn.cy.proxy.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * 数据从下游透传至上游
 * <p>
 * 每个 {@link io.netty.channel.Channel} 对应一个此对象
 */
public class PassThrough2UpstreamHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }
}
