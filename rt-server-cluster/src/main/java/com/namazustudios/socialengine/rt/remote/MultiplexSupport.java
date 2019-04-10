package com.namazustudios.socialengine.rt.remote;

import java.util.UUID;

public interface MultiplexSupport {

    /**
     * Makes a type 3 named {@link UUID} from the supplied node id using the appropriate charset encoding.  The returned
     * {@link UUID} can then be used to assign destination routes using {@link #open(UUID)}.
     *
     * @param destinationNodeId the destination node id
     * @return the {@link UUID} of the destination
     */
    UUID getDestinationUUIDForNodeId(String destinationNodeId);

    /**
     * Adds a {@link UUID} for a particular destination.
     *
     * @param destination the {@link UUID} destination
     */
    void open(UUID destination);

    /**
     * Removes {@link UUID} for a particular destination.
     *
     * @param destination the {@link UUID} destination
     */
    void close(UUID destination);

    /**
     * Opens a route to the supplied destination node ID.
     *
     * @param destinationNodeId the destination node id
     */
    default void open(final String destinationNodeId) {
        final UUID destination = getDestinationUUIDForNodeId(destinationNodeId);
        open(destination);
    }

    /**
     * Closes a route to the supplied destination node ID.
     *
     * @param destinationNodeId the destination node id
     */
    default void close(final String destinationNodeId) {
        final UUID destination = getDestinationUUIDForNodeId(destinationNodeId);
        close(destination);
    }

    void connect(final String connectAddress);

    void disconnect(final String connectAddress);

}
