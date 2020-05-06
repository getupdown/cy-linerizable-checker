package cn.cy.proxy.core;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.base.Preconditions;

/**
 * DownstreamDescriptorManager
 */
@NotThreadSafe
public class DownstreamDescriptorManager {

    private static final DownstreamDescriptorManager instance = new DownstreamDescriptorManager();

    private DownstreamDescriptorManager() {
        descriptorMaps = new HashMap<>();
    }

    public static DownstreamDescriptorManager getInstance() {
        return instance;
    }

    // downstreamAddr -> descriptor
    private Map<InetSocketAddress, DownstreamDescriptor> descriptorMaps;

    public void register(InetSocketAddress downstreamAddr, DownstreamDescriptor descriptor) {

        if (descriptorMaps.containsKey(downstreamAddr)) {
            return;
        }

        DownstreamDescriptor prev = descriptorMaps.putIfAbsent(downstreamAddr, descriptor);

        Preconditions.checkState(prev == null);
    }

    public DownstreamDescriptor get(InetSocketAddress addr) {
        return descriptorMaps.getOrDefault(addr, null);
    }
}
