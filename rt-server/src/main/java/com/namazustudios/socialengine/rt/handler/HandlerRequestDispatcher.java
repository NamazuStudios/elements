package com.namazustudios.socialengine.rt.handler;

import com.namazustudios.socialengine.rt.*;

/**
 *
 * The logical entry point for the Container.  This combines the functionality
 * of the {@link ResourceService}, {@link HandlerFilter}, and {@link ExceptionMapper}
 * to process each request exchanged through the RT server.
 *
 * Created by patricktwohig on 7/27/15.
 */
public interface HandlerRequestDispatcher {

    /**
     * Handles a request.  This is responsible for finding the appropriate
     * path handler.  This method does everything it can to avoid all
     * exceptions, and uses the worker mapping of {@link ExceptionMapper}
     * instances to relay faults to the clients.
     *
     * This ensures that the given {@link Request} is properly handled through
     * each of the servers's configured {@link HandlerFilter} instances and ultimately
     * ends up in the correct {@link ClientRequestHandler} supplied by the configurd
     * {@link ResourceService}.
     *
     * @param handlerClientSession the handlerClientSession the handlerClientSession making the request
     * @param request the request the request itself
     * @param responseReceiver
     */
    void dispatch(HandlerClientSession handlerClientSession, Request request, ResponseReceiver responseReceiver);

}
