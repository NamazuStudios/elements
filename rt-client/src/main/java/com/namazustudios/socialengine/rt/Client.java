package com.namazustudios.socialengine.rt;

import java.util.function.Consumer;

/**
 * The main interface for the {@link Client}.
 *
 * Created by patricktwohig on 9/3/15.
 */
public interface Client {

    /**
     * Sends the request to the server.  This will block until the server provides the
     * {@link Response}.  There is no way to cancel the pending request.
     *
     * @param request the request
     * @param expectedType the expected type
     * @return the {@link Response}
     */
    Response sendRequest(Request request, Class<?> expectedType);

    /**
     * Sends the request to the server.  The response will be handed to the given
     * {@link Consumer<Response>} some time later.  If the request times out, then
     * a special response will be generated client side indicating the error.
     *
     * @param request the request
     * @param receiver the response
     *
     * @return an instance of {@link PendingRequest} which can be used to cancel the operation
     */
    PendingRequest sendRequest(Request request, Class<?> expectedType, Consumer<Response> receiver);

    /**
     * Subscribes to events at the given path and name, with the given
     * {@link EventReceiver}.  Note that this does nothing to make any actual
     * calls server side, as subscribing to events is something that is performed
     * on the server.  This will, however, map any incoming event to the
     * appropriate location.
     *
     * @param path the path
     * @param name the name
     * @param eventReceiver the even treceiver
     * @param <EventT> the event type
     * @return an instance ov {@link Observation}
     */
    <EventT> Observation observe(Path path, String name, EventReceiver<EventT> eventReceiver);

}
