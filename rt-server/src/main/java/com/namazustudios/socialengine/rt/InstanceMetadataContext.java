package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.Proxyable;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;

import java.util.Set;

/**
 * Provides data for an Instance.
 */
@Proxyable
public interface InstanceMetadataContext {
    @RemotelyInvokable(AddressedRoutingStrategy.class)
    Object getNetworkAddressAlias();

    @RemotelyInvokable(AddressedRoutingStrategy.class)
    Set<NodeId> getAllNodeIds();

    @RemotelyInvokable(AddressedRoutingStrategy.class)
    double getLoadAverage();

    /**
     * Starts this {@link NodeMetadataContext}.
     */
    default void start() {}

    /**
     * Stops this {@link NodeMetadataContext}.
     */
    default void stop() {}
}
