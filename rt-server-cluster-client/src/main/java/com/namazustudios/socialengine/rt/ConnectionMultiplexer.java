package com.namazustudios.socialengine.rt;

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

}
