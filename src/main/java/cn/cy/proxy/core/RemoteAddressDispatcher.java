package cn.cy.proxy.core;

import java.net.InetSocketAddress;

/**
 * RemoteAddressDispatcher
 */
public interface RemoteAddressDispatcher {

    /**
     * decide which one to connect by downstream address
     *
     * @param downstreamAddr
     *
     * @return
     */
    InetSocketAddress decideUpstreamRemoteAddr(InetSocketAddress downstreamAddr);

}
