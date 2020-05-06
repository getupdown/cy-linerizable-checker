package cn.cy.proxy.core;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 描述upstream的信息, 一个ip:port二元组对应一个此对象
 */
public class UpstreamDescriptorImpl implements UpstreamDescriptor {

    /**
     * 一个{@link Bootstrap}对象, 对应一个remoteAddress
     */
    private Bootstrap bootstrap;

    private InetSocketAddress upstreamAddress;

    private ChannelPoolMap<InetSocketAddress, FixedChannelPool> channelPoolMap;

    public UpstreamDescriptorImpl(InetSocketAddress upstreamAddress) {

        this.bootstrap = initUpstreamBootstrap(upstreamAddress);
        this.upstreamAddress = upstreamAddress;
        this.channelPoolMap = new DownstreamAddrBasedCPMap(bootstrap);

    }

    private Bootstrap initUpstreamBootstrap(InetSocketAddress upstreamAddress) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                // 禁用nagle算法
                .option(ChannelOption.TCP_NODELAY, true)
                // 由于在channelPool内部逻辑中, 会把bootStrap的handle给覆盖掉
                // 所以这里无需申明handle方法的channelInitializer
                .remoteAddress(upstreamAddress);

        return bootstrap;
    }

    @Override
    public InetSocketAddress getUpstreamAddr() {
        return upstreamAddress;
    }

    @Override
    public ChannelPool getChannelPoolByDownstreamAddr(InetSocketAddress downstreamAddr) {
        return channelPoolMap.get(downstreamAddr);
    }
}
