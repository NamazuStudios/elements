package com.namazustudios.socialengine.rt;

/**
 * Informs the underlying application, contained in the {@link Node}, of the various livecycle events.
 */
public interface NodeLifecycle {

    /**
     * Starts the lifecycle.
     */
    void preStart();

    /**
     * Shutdowns or stops the lifecycle.
     */
    void postStop();

}
