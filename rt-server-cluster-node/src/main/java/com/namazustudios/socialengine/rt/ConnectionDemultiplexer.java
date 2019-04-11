package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.remote.MultiplexSupport;

import java.util.UUID;

/**
 * Routes incoming connections to the appropriate {@link Node}.  The implementation of this interface should openInprocChannel a
 * listener (such as a socket), accept incoming connections, and route them to the appropriate {@link Node}.  This pairs
 * with the appropriate multiplexer on the other end.
 */
public interface ConnectionDemultiplexer extends MultiplexSupport {

    /**
     * Starts this {@link ConnectionDemultiplexer} binds and opens any necessary connections, waits for connections, and routes
     * them to the appropriate internal {@link Node}.
     */
    void start();

    /**
     * Stops this {@link ConnectionDemultiplexer}, closing any connections.  This may allow connections to complete gracefully
     * however, when this method returns no further connections may be made and all network activity should have ceased
     * through this {@link ConnectionDemultiplexer}
     */
    void stop();

    /**
     * Gets the bind address for the inprocIdentifier with the supplied {@link UUID}.
     *
     * @param uuid the uuid
     * @return the openBackendChannel address
     */
    String getBindAddress(final UUID uuid);

}
