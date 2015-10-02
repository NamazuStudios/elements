package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.NotFoundException;

/**
 * Handles the underlying mechanisms by which data is shuffled across the network.  This underlying
 * network connector can be provided an implementation of this which will feed data to the owning
 * {@link Client} instance.
 *
 * Created by patricktwohig on 9/4/15.
 */
public interface IncomingNetworkOperations {

    /**
     * Gets the expected payload type for hte given {@link ResponseHeader}.  The underlying network
     * components will deserialize the incoming object into a new isntance of the supplied type.
     *
     * If, for some reason that the type cannot be resolved (such as a cancelled request), then the
     * calling code must properly handle the {@link NotFoundException}.
     *
     * @param responseHeader the {@link ResponseHeader}
     * @return the type, never null
     * @throws {@link NotFoundException} if the type cannot be resolved, typically when the associated request was cancelled
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
     *  @param event the event
     *
     */
    void receive(final Event event);

}
