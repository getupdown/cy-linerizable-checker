package cn.cy.proxy.core;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.cy.proxy.handler.PassThrough2UpstreamHandler;
import cn.cy.proxy.handler.WriteBack2DownstreamHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;

/**
 * DownstreamAddrBasedCPMap
 */
public class DownstreamAddrBasedCPMap extends AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool> {

    private final Bootstrap upstreamBootStrap;

    public DownstreamAddrBasedCPMap(Bootstrap upstreamBootStrap) {
        this.upstreamBootStrap = upstreamBootStrap;
    }

    @Override
    protected FixedChannelPool newPool(InetSocketAddress downstreamAddr) {
        return new FixedChannelPool(upstreamBootStrap, new CPHandler(downstreamAddr), 1);
    }

    /**
     * 在SimpleChannelPool的构造函数中, 以传入的bootStrap参数
     * 初始化一个 {@link SimpleChannelPool} 自己的 {@link io.netty.channel.ChannelInitializer}
     * 其中会调用 {@link ChannelPoolHandler#channelCreated(Channel)}, 所以我们把初始化handler的逻辑写在这个channelCreated方法中
     * <p>
     * {@link FixedChannelPool} 是他的子类
     *
     * @see SimpleChannelPool#SimpleChannelPool(io.netty.bootstrap.Bootstrap, io.netty.channel.pool.ChannelPoolHandler,
     * io.netty.channel.pool.ChannelHealthChecker, boolean, boolean)
     */
    private static class CPHandler implements ChannelPoolHandler {

        public static final Logger LOGGER = LoggerFactory.getLogger(CPHandler.class);

        private final InetSocketAddress downstreamAddr;

        public CPHandler(InetSocketAddress downstreamAddr) {
            this.downstreamAddr = downstreamAddr;
        }

        @Override
        public void channelReleased(Channel ch) throws Exception {
            LOGGER.info("connection released ! ch : {}", ch);
        }

        @Override
        public void channelAcquired(Channel ch) throws Exception {
            LOGGER.info("connection acquired ! ch : {}", ch);
        }

        @Override
        public void channelCreated(Channel ch) throws Exception {

            LOGGER.info("connection created ! ch : {}", ch);

            ch.pipeline().addLast(
                    new PassThrough2UpstreamHandler(),
                    new WriteBack2DownstreamHandler(
                            DownstreamDescriptorManager.getInstance().get(downstreamAddr).getDownstreamChannel())
            );
        }
    }
}
