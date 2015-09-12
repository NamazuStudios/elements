package com.namazustudios.socialengine.rt;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Represents the entry point for either a client or server.  This will start up, bind, and drive
 * the underlying connection until it is no longer needed.
 *
 * Created by patricktwohig on 9/11/15.
 */
public interface Connector {

    /**
     * Binds and runs the underling service.  This will block until until the service has shut down.
     */
    void bindAndRun(final SocketAddress socketAddress);

}
