package com.namazustudios.socialengine.rt;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * Created by patricktwohig on 9/4/15.
 */
public interface OutgoingNetworkOperations {

    /**
     * This will dispatch the given {@link Request} over the network.  The request is simply send through
     * the underlying transport and it is the responsibility of the calling code to track the request.
     *
     * This class is used by the instances of {@link Client} to send outgoing requests
     *
     * @param request the request
     * @throws IllegalStateException if the instance has been closed.
     *
     */
    void dispatch(final Request request);

}
