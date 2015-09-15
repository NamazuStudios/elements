package com.namazustudios.socialengine.rt;

import java.net.SocketAddress;

/**
 * Created by patricktwohig on 9/12/15.
 */
public interface ClientContainer {

    /**
     * Connects to the given server.
     *
     * @param socketAddress
     */
    void connect(final SocketAddress socketAddress);

    /**
     * Disconnects from the server.
     */
    void disconnect();

}
