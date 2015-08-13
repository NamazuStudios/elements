package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.rt.*;

/**
 *
 * The logical entry point for the Server.  This combines the functionality
 * of the {@link ResourceService}, {@link EdgeFilter}, and {@link ExceptionMapper}
 * to process each request exchanged through the RT server.
 *
 * Created by patricktwohig on 7/27/15.
 */
public interface EdgeRequestDispatcher {

    /**
     * Handles a request.  This is responsible for finding the appropriate
     * path handler.  This method does everything it can to avoid all
     * exceptions, and uses the internal mapping of {@link ExceptionMapper}
     * instances to relay faults to the clients.
     *
     * This ensures that the given {@link Request} is properly handled through
     * each of the servers's configured {@link EdgeFilter} instances and ultimately
     * ends up in the correct {@link EdgeRequestPathHandler} supplied by the configurd
     * {@link ResourceService}.
     *
     * @param client the client the client making the request
     * @param request the request the request itself
     * @param edgeResponseReceiver
     */
    void dispatch(Client client, Request request, EdgeResponseReceiver edgeResponseReceiver);

}
