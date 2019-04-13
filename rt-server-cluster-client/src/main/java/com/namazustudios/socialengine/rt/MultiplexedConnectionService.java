package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.remote.MultiplexSupport;

import java.util.UUID;

/**
 * Routes many incoming connections into a single connection.  Works in tandem with a demultiplexer on the other end.
 */
public interface MultiplexedConnectionService extends MultiplexSupport {

    /**
     * Starts this {@link MultiplexedConnectionService} and begins monitoring for SRV records, thereby identifying
     * app node instances in the network to which the manager should issueOpenBackendChannelCommand.
     */
    void start();

    /**
     * Stops this {@link MultiplexedConnectionService}, closing any connections.  This may allow connections to complete gracefully
     * however, when this method returns no further connections may be made and all network activity should have ceased
     * through this {@link MultiplexedConnectionService}
     */
    void stop();

    /**
     * Gets the issueOpenBackendChannelCommand address for the inproc with the supplied {@link UUID}.
     *
     * @param inprocIdentifier the uuid
     * @return the issueOpenBackendChannelCommand address
     */
    String getInprocConnectAddress(final UUID inprocIdentifier);

}
