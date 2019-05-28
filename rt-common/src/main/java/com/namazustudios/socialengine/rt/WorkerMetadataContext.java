package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.Proxyable;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;

/**
 * Provides data for a Worker.
 */
@Proxyable
public interface WorkerMetadataContext {
    @RemotelyInvokable(AddressedRoutingStrategy.class)
    WorkerId getWorkerId();

    @RemotelyInvokable(AddressedRoutingStrategy.class)
    long getInMemoryResourceCount();

    // TODO: this is an instance-level piece of data, so maybe split out the instance-level data into a separate
    //  context, e.g. InstanceMetadataContext. And we could report instance-level resource counts etc.
    @RemotelyInvokable(AddressedRoutingStrategy.class)
    double getLoadAverage();

    /**
     * Starts this {@link WorkerMetadataContext}.
     */
    default void start() {}

    /**
     * Stops this {@link WorkerMetadataContext}.
     */
    default void stop() {}
}
