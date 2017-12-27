package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.rt.Node;

/**
 * Routes incoming connections to the appropriate {@link Node}.  The implementation of this interface should open a
 * listener (such as a socket), and accept incoming connections and routing them to the {@link Node}.  The specifics of
 * how this particular functionality is an implementation detail, and may not exactly use a socket.
 */
public interface ConnectionRouter {

    /**
     * Starts this {@link ConnectionRouter} binds and opens any necessary connections, waits for connections, and routes
     * them to the appropriate internal {@link Node}.
     */
    void start();

    /**
     * Stops this {@link ConnectionRouter}, closing any connections.  This may allow connections to complete gracefully
     * however, when this method returns no further connections may be made and all network activity should have ceased
     * through this {@link ConnectionRouter}
     */
    void stop();

}
