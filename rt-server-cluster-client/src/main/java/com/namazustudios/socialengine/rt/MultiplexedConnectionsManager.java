package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.remote.MultiplexSupport;

import java.util.UUID;

/**
 * Routes many incoming connections into a single connection.  Works in tandem with a demultiplexer on the other end.
 */
public interface MultiplexedConnectionsManager extends MultiplexSupport {

    /**
     * Starts this {@link MultiplexedConnectionsManager} and begins monitoring for SRV records, thereby identifying
     * app node instances in the network to which the manager should connect.
     */
    void start();

    /**
     * Stops this {@link MultiplexedConnectionsManager}, closing any connections.  This may allow connections to complete gracefully
     * however, when this method returns no further connections may be made and all network activity should have ceased
     * through this {@link MultiplexedConnectionsManager}
     */
    void stop();

    /**
     * Gets the connect address for the inprocIdentifier with the supplied {@link UUID}.
     *
     * @param uuid the uuid
     * @return the connect address
     */
    String getConnectAddress(final UUID uuid);

}
