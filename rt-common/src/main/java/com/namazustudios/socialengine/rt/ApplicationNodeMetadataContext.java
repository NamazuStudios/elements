package com.namazustudios.socialengine.rt;

import java.util.UUID;

/**
 * Provides data for Application Nodes in the network.
 */
public interface ApplicationNodeMetadataContext {

    /**
     * Starts this {@link ApplicationNodeMetadataContext}.
     */
    default void startWithSingleUseSelectionStrategy(SingleUseSelectionStrategy singleUseSelectionStrategy) {}

    /**
     * Stops this {@link ApplicationNodeMetadataContext}.
     */
    default void stop() {}

    UUID getLocalApplicationNodeUuid();

    UUID getNextApplicationNodeUuidForSingleUseInvocation();

    enum SingleUseSelectionStrategy {
        /**
         *  Select an Application Node in the network for single use invocation randomly.
         */
        RANDOM,

        /**
         *  Select by an un-prioritized, circular schedule.
         */
        ROUND_ROBIN,

        /**
         *  Select by the Application Node that has most recently reported the least number of allocated resources.
         */
        MIN_RESOURCE_ALLOCATION,

        /**
         *  Select by the Application Node that has most recently reported the smallest load average.
         */
        MIN_LOAD_AVERAGE,
    }

}
