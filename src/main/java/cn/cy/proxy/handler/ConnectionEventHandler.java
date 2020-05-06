package cn.cy.proxy.handler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.cy.proxy.core.DownstreamDescriptor;
import cn.cy.proxy.core.DownstreamDescriptorImpl;
import cn.cy.proxy.core.DownstreamDescriptorManager;
import cn.cy.proxy.core.RemoteAddressDispatcher;
import cn.cy.proxy.core.UpstreamDescriptor;
import cn.cy.proxy.core.UpstreamDescriptorImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.pool.ChannelPool;

/**
 * ConnectionEventHandler
 */
public class ConnectionEventHandler extends ChannelInboundHandlerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConnectionEventHandler.class);

    private final RemoteAddressDispatcher remoteAddressDispatcher;

    private Map<SocketAddress, UpstreamDescriptor> upstreamMap = new HashMap<>();

    public ConnectionEventHandler(RemoteAddressDispatcher remoteAddressDispatcher) {
        this.remoteAddressDispatcher = remoteAddressDispatcher;
    }

    /**
     * 在downstream连接到proxy的时候发生(三次握手完毕之后)
     * 注意, 这个方法是在连接建立完毕之后, 异步执行的
     *
     * @param ctx
     *
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("connection active! {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        InetSocketAddress upstreamAddr = decideUpstreamAddr(ctx);

        UpstreamDescriptor ud = null;

        synchronized(this) {
            // 注册upstreamDescriptor
            ud = registerUpstreamDescriptorIfNotExist(upstreamAddr);

            // 注册downstream
            registerDownstreamDescriptorIfNotExist(ctx);
        }

        // 通过FixedChannelPool设置maxConnection, 保证一个下游连接最多同时占用一个上游连接
        ChannelPool cp = ud.getChannelPoolByDownstreamAddr((InetSocketAddress) ctx.channel().remoteAddress());

        Future<Channel> f = cp.acquire();

        // 交付至upstream的channel
        Channel upstreamChannel = f.get();

        // write and flush
        upstreamChannel.writeAndFlush(msg).addListener(future -> {
            if (!future.isSuccess()) {
                LOGGER.warn("future is not success! cause ", future.cause());
            }
            cp.release(upstreamChannel);
        });
    }

    private InetSocketAddress decideUpstreamAddr(ChannelHandlerContext ctx) {
        SocketAddress downstreamAddr = ctx.channel().remoteAddress();
        return remoteAddressDispatcher.decideUpstreamRemoteAddr((InetSocketAddress) downstreamAddr);
    }

    private UpstreamDescriptor registerUpstreamDescriptorIfNotExist(InetSocketAddress upstreamAddr) {

        UpstreamDescriptor ud = null;

        if (!upstreamMap.containsKey(upstreamAddr)) {
            UpstreamDescriptorImpl upstreamDescriptor =
                    new UpstreamDescriptorImpl(upstreamAddr);

            ud = upstreamMap.putIfAbsent(upstreamAddr, upstreamDescriptor);

            if (ud == null) {
                ud = upstreamDescriptor;
            }

        } else {
            ud = upstreamMap.get(upstreamAddr);
        }

        return ud;
    }

    private DownstreamDescriptor registerDownstreamDescriptorIfNotExist(ChannelHandlerContext downstreamContext) {
        DownstreamDescriptorImpl downstreamDescriptor =
                new DownstreamDescriptorImpl((InetSocketAddress) downstreamContext.channel().remoteAddress(),
                        downstreamContext.channel());

        DownstreamDescriptorManager.getInstance().register((InetSocketAddress) downstreamContext
                .channel().remoteAddress(), downstreamDescriptor);

        return downstreamDescriptor;
    }

}
