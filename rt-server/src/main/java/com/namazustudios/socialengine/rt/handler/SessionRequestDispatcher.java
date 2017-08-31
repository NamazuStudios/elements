package com.namazustudios.socialengine.rt.handler;

import com.namazustudios.socialengine.rt.*;

/**
 *
 * The logical entry point for the Container.  This combines the functionality
 * of the {@link ResourceService}, {@link Filter}, and {@link ExceptionMapper}
 * to process each request exchanged through the RT server.
 *
 * Created by patricktwohig on 7/27/15.
 */
@FunctionalInterface
public interface SessionRequestDispatcher {

    /**
     * Handles a request.  This is responsible for finding the appropriate
     * path handler.  This method does everything it can to avoid all
     * exceptions, and uses the worker mapping of {@link ExceptionMapper}
     * instances to relay faults to the clients.
     *
     * This ensures that the given {@link Request} is properly handled through
     * each of the servers's configured {@link Filter} instances and ultimately
     * ends up in the correct {@link ClientRequestHandler} supplied by the configurd
     * {@link ResourceService}.
     *
     * @param session the session the session making the request
     * @param request the request the request itself
     * @param responseReceiver
     */
    void dispatch(Session session, Request request, ResponseReceiver responseReceiver);

}
