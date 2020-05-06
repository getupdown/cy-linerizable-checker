package cn.cy.proxy.core;

import java.net.InetSocketAddress;

/**
 * ConfigurableAddressDispatcher
 */
public class ConfigurableAddressDispatcher implements RemoteAddressDispatcher {

    @Override
    public InetSocketAddress decideUpstreamRemoteAddr(InetSocketAddress downstreamAddr) {
        return InetSocketAddress.createUnresolved("118.31.4.42", 2181);
    }
}
