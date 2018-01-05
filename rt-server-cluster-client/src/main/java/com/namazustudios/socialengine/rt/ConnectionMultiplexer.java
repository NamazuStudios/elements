package com.namazustudios.socialengine.rt;

import java.util.UUID;

/**
 * Routes many incoming connections into a single connection.  Works in tandem wiht a demultiplxer on the other end.
 */
public interface ConnectionMultiplexer {

    /**
     * Starts this {@link ConnectionMultiplexer} connects to any remote endpoints and then begins routing connections
     * to the remote end.
     */
    void start();

    /**
     * Stops this {@link ConnectionMultiplexer}, closing any connections.  This may allow connections to complete gracefully
     * however, when this method returns no further connections may be made and all network activity should have ceased
     * through this {@link ConnectionMultiplexer}
     */
    void stop();

    /**
     * Makes a type 3 named {@link UUID} from the supplied node id using the appropriate charset encoding.  The returned
     * {@link UUID} can then be used to assign destination routes using {@link #open(UUID)}.
     *
     * @param destinationNodeId the destination node id
     * @return the {@link UUID} of the destination
     */
    UUID getDestinationUUIDForNodeId(final String destinationNodeId);

    /**
     * Gets the connect address for the destination with the supplied {@link UUID}.
     *
     * @param uuid the uuid
     * @return the connect address
     */
    String getConnectAddress(final UUID uuid);

    /**
     * Opens a route to the supplied destination {@link UUID}.
     *
     * @param destinationNodeId the destination node id
     */
    default void open(final String destinationNodeId) {
        final UUID destination = getDestinationUUIDForNodeId(destinationNodeId);
        open(destination);
    }

    /**
     * Closes a route to the supplied destination {@link UUID}.
     *
     * @param destinationNodeId the destination node id
     */
    default void close(final String destinationNodeId) {
        final UUID destination = getDestinationUUIDForNodeId(destinationNodeId);
        close(destination);
    }

    /**
     * Adds a {@link UUID} for a particular destination.
     *
     * @param destination the {@link UUID} destination
     */
    void open(final UUID destination);

    /**
     * Removes {@link UUID} for a particular destination.
     *
     * @param destination the {@link UUID} destination
     */
    void close(final UUID destination);

}
