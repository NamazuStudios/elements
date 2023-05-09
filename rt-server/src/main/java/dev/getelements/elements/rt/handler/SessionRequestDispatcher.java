package dev.getelements.elements.rt.handler;

import dev.getelements.elements.rt.ExceptionMapper;
import dev.getelements.elements.rt.Request;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.Response;

import java.util.function.Consumer;

/**
 *
 * The logical entry point for the Scheduler.  This combines the functionality of the {@link ResourceService},
 * {@link Filter}, and {@link ExceptionMapper} to process each request exchanged through the RT server.
 *
 * This may accept a specific type of {@link Request}, as specified by the argument RequestT generic argument,
 * but it is the responsibility of the enclosing container to handle the {@link Response} as it sees fit.
 *
 * Created by patricktwohig on 7/27/15.
 */
@FunctionalInterface
public interface SessionRequestDispatcher<RequestT extends Request> {

    /**
     * Handles a request.  This is responsible for finding the appropriate path handler.  This method does everything
     * it can to avoid all exceptions, and uses the worker mapping of {@link ExceptionMapper} instances to relay
     * faults to the connected client instances.
     *
     * This ensures that the given {@link Request} is properly handled through each of the servers's configured
     * {@link Filter} instances and ultimately ends up in the correct by the correct {@link ResourceService}.
     *
     * It is possible that a {@link Filter} specified in the chain may intercept the {@link RequestT}, in which case
     * it will never be handed to any particular {@link ResourceService}
     *
     * @param session the session the session making the request
     * @param request the request the request itself
     * @param responseReceiver
     */
    void dispatch(Session session, RequestT request, Consumer<Response> responseReceiver);

}
