package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.Proxyable;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;

import java.util.UUID;

/**
 * Provides data for an Application node.
 */
@Proxyable
public interface ApplicationNodeMetadataContext {

    @RemotelyInvokable
    UUID getUuid();

    @RemotelyInvokable
    long getInMemoryResourceCount();

    @RemotelyInvokable
    double getLoadAverage();

    /**
     * Starts this {@link ApplicationNodeMetadataContext}.
     */
    default void start() {}

    /**
     * Stops this {@link ApplicationNodeMetadataContext}.
     */
    default void stop() {}

    // TODO: VV Move all this to another module VV
//    UUID getLocalApplicationNodeUuid();
//
//    UUID getNextApplicationNodeUuidForSingleUseInvocation();
//
//    String getHostnameForAppNodeUUID(uuid);
//
//    enum SingleUseSelectionStrategy {
//        /**
//         *  Select an Application Node in the network for single use invocation randomly.
//         */
//        RANDOM,
//
//        /**
//         *  Select by an un-prioritized, circular schedule.
//         */
//        ROUND_ROBIN,
//
//        /**
//         *  Select by the Application Node that has most recently reported the least number of allocated resources.
//         */
//        MIN_RESOURCE_ALLOCATION,
//
//        /**
//         *  Select by the Application Node that has most recently reported the smallest load average.
//         */
//        MIN_LOAD_AVERAGE,
//    }

}
