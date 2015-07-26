package com.namazustudios.socialengine.rt;

/**
 * Represents the currently connected clients and provides a means to
 * dispatch messages to clients.
 *
 * Created by patricktwohig on 7/26/15.
 */
public interface ConnectedClientService {

    /**
     * Used to indicate that a client has connected.
     *
     * @param client the client
     */
    void addConnection(final Client client);

    /**
     * Removes a connection from the pool.
     *
     * @param client the client
     */
    void removeConnection(final Client client);

    /**
     * Gets the {@link Receiver} instance which can be used to receive messages
     * of the given type.
     *
     * @param client the client
     * @param payloadTClass the payload type
     * @param <PayloadT> the payload type
     *
     * @return the {@link Receiver} which will receive the response
     */
    <PayloadT> Receiver<Response, PayloadT> getResponseReceiver(final Client client, Class<PayloadT> payloadTClass);

    /**
     * Gets the {@link Receiver} instance which can be used to receive events
     * of the given type.
     *
     * @param client the client
     * @param payloadTClass the payload type
     * @param <PayloadT> the payload type
     *
     * @return the {@link Receiver} instance which will event
     */
    <PayloadT> Receiver<Event, PayloadT> getEventReceiver(final Client client, Class<PayloadT> payloadTClass);

}
