package cn.cy.proxy.core;

import java.net.InetSocketAddress;

import io.netty.channel.pool.ChannelPool;

/**
 * UpstreamDescriptor
 */
public interface UpstreamDescriptor {

    InetSocketAddress getUpstreamAddr();

    ChannelPool getChannelPoolByDownstreamAddr(InetSocketAddress downstreamAddr);

}
