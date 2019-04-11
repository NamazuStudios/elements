package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.remote.MultiplexSupport;

import java.util.UUID;

/**
 * Routes many incoming connections into a single connection.  Works in tandem with a demultiplexer on the other end.
 */
public interface MultiplexedConnectionManager extends MultiplexSupport {

    /**
     * Starts this {@link MultiplexedConnectionManager} and begins monitoring for SRV records, thereby identifying
     * app node instances in the network to which the manager should openBackendChannel.
     */
    void start();

    /**
     * Stops this {@link MultiplexedConnectionManager}, closing any connections.  This may allow connections to complete gracefully
     * however, when this method returns no further connections may be made and all network activity should have ceased
     * through this {@link MultiplexedConnectionManager}
     */
    void stop();

    /**
     * Gets the openBackendChannel address for the inproc with the supplied {@link UUID}.
     *
     * @param inprocIdentifier the uuid
     * @return the openBackendChannel address
     */
    String getInprocConnectAddress(final UUID inprocIdentifier);

}
