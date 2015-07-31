package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.NotFoundException;

/**
 * Represents the currently connected clients and provides a means to
 * dispatch messages to clients.
 *
 * Created by patricktwohig on 7/26/15.
 */
public interface ConnectedClientService {

    /**
     * Gets the {@link Receiver} instance which can be used to receive messages
     * of the given type.
     *
     * @param client the client
     * @param payloadTClass the payload type
     * @param <PayloadT> the payload type
     *
     * @return the {@link Receiver} which will receive the response
     *
     * @throws {@link NotFoundException} if no active connection for the client can be found
     */
    <PayloadT> Receiver<ResponseHeader, PayloadT> getResponseReceiver(final Client client, Class<PayloadT> payloadTClass);

    /**
     * Gets the {@link Receiver} instance which can be used to receive events
     * of the given type.
     *
     * @param client the client
     * @param payloadTClass the payload type
     * @param <PayloadT> the payload type
     *
     * @return the {@link Receiver} instance which will event
     * @throws {@link NotFoundException} if no active connection for the client can be found
     */
    <PayloadT> Receiver<Event, PayloadT> getEventReceiver(final Client client, Class<PayloadT> payloadTClass);

}
