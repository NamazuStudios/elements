package com.namazustudios.socialengine.rt;

/**
 * Routes incoming connections to the appropriate {@link Node}.  The implementation of this interface should open a
 * listener (such as a socket), accept incoming connections, and route them to the appropriate {@link Node}.  This pairs
 * with the appropriate multiplexer on the other end.
 */
public interface ConnectionDemultiplexer {

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

}
