package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.Proxyable;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;
import com.namazustudios.socialengine.rt.remote.RoutingStrategy;

import java.util.UUID;

/**
 * Provides data for an Application node.
 */
@Proxyable
public interface ApplicationNodeMetadataContext {
    @RemotelyInvokable(AddressedRoutingStrategy.class)
    UUID getUuid();

    @RemotelyInvokable(AddressedRoutingStrategy.class)
    long getInMemoryResourceCount();

    @RemotelyInvokable(AddressedRoutingStrategy.class)
    double getLoadAverage();

    /**
     * Starts this {@link ApplicationNodeMetadataContext}.
     */
    default void start() {}

    /**
     * Stops this {@link ApplicationNodeMetadataContext}.
     */
    default void stop() {}
}
