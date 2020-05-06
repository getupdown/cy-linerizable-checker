package cn.cy.proxy.core;

import java.net.InetSocketAddress;

import io.netty.channel.Channel;

/**
 * DownstreamDescriptor
 */
public interface DownstreamDescriptor {

    InetSocketAddress getDownstreamAddr();

    Channel getDownstreamChannel();

}
