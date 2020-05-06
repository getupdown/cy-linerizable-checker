package cn.cy.proxy.core;

import java.net.InetSocketAddress;

import io.netty.channel.Channel;

/**
 * DownstreamDescriptorImpl
 */
public class DownstreamDescriptorImpl implements DownstreamDescriptor {

    private final InetSocketAddress downstreamAddr;

    private final Channel downstreamChannel;

    public DownstreamDescriptorImpl(InetSocketAddress downstreamAddr, Channel downstreamChannel) {
        this.downstreamAddr = downstreamAddr;
        this.downstreamChannel = downstreamChannel;
    }

    @Override
    public InetSocketAddress getDownstreamAddr() {
        return downstreamAddr;
    }

    @Override
    public Channel getDownstreamChannel() {
        return downstreamChannel;
    }
}
