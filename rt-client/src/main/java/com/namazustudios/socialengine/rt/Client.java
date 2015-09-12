package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.NotFoundException;

/**
 * The main interface for the {@link Client}.
 *
 * Created by patricktwohig on 9/3/15.
 */
public interface Client {

    /**
     * Sends the request to the server.  The response will be handed to the give
     * {@link ResponseReceiver} some time later.  If the request times out, then
     * a special response will be generated client side indicating the error.
     *
     * @param request the request
     * @param receiver the response
     *
     * @return an instance of {@link PendingRequest} which can be used to cancel the operation
     */
    PendingRequest sendRequest(Request request, Class<?> expectedType, ResponseReceiver receiver);

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
     * @return an instance ov {@link Subscription}
     */
    <EventT> Subscription subscribe(Path path, String name, EventReceiver<EventT> eventReceiver);

    /**
     * Handles the underlying mechanisms by which data is shuffled across the network.  This underlying
     * network connector can be provided an implementation of this which will feed data to the owning
     * {@link Client} instance.
     *
     * Created by patricktwohig on 9/4/15.
     */
    interface NetworkOperations {

        /**
         * Gets the expected payload type for hte given {@link ResponseHeader}.  The underlying network
         * components will deserialize the incoming object into a new isntance of the supplied type.
         *
         * @param responseHeader the {@link ResponseHeader}
         * @return the type, never null
         * @throws {@link NotFoundException} if the type cannot be resolved, typically when the assocaited request was cancelled
         */
        Class<?> getPayloadType(final ResponseHeader responseHeader);

        /**
         * Receives the instance of the given {@link Response}.  The {@link Response#getPayload()} method
         * will return an instance specified by the matching {@link #getPayloadType(ResponseHeader)}.
         *
         * This is typically determined by matching the {@link RequestHeader#getSequence()} to the
         * {@link ResponseHeader#getSequence()} method.
         *
         * @param response the response
         */
        void receive(final Response response);

        /**
         * Gets the {@link Class} of all types into which the payload for an {@link Event} will be
         * deserialized.
         *
         * If there are no hanlders configured, then the returned {@link Iterable} must return
         * an {@link Iterable} over no objects (ie empty set).  IN which case the event will be
         * ignored and a warning logged for the dead event.
         *
         * @param eventHeader
         * @return an {@link Iterable} of {@link Class}es for events, never null.
         */
        Iterable<Class<?>> getEventTypes(final EventHeader eventHeader);

        /**
         * For each instance of {@link Class} retured by {@link #getEventTypes(EventHeader)}, this will
         * return be called exactly once with the given {@link Event}.
         *
         * @param event the event
         * @param eventType the type which will match {@link Event#getPayload()}
         */
        void receive(final Event event, final Class<?> eventType);

    }

}
