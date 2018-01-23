package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.remote.MultiplexSupport;

import java.util.UUID;

/**
 * Routes many incoming connections into a single connection.  Works in tandem wiht a demultiplxer on the other end.
 */
public interface ConnectionMultiplexer extends MultiplexSupport {

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
     * Gets the connect address for the destination with the supplied {@link UUID}.
     *
     * @param uuid the uuid
     * @return the connect address
     */
    String getConnectAddress(final UUID uuid);

}
