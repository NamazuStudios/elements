package com.namazustudios.socialengine.rt.remote;

/**
 * Informs the underlying application, contained in the {@link Node}, of the various livecycle events.
 */
public interface NodeLifecycle {

    /**
     * Called before the {@link Node} is up and accepting connections
     * @param node
     */
    default void nodePreStart(final Node node) {}

    /**
     * Called after the {@link Node} is up and accepting connections.
     * @param node
     */
    default void nodePostStart(final Node node) {}

    /**
     * Called just before the {@link Node} will stop accepting connections and shut down
     * @param node
     */
    default void nodePreStop(final Node node) {}

    /**
     * Called just after the {@link Node} has stopped accepting connections.
     * @param node
     */
    default void nodePostStop(final Node node) {}

}
