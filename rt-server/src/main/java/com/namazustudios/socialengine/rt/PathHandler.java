package com.namazustudios.socialengine.rt;

/**
 * An instance of PathHandler is responsible for producing {@link ResponseHeader} objects
 * from instances of {@link RequestHeader}.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface PathHandler<PayloadT> {

    /**
     * Gets the {@link Class} which can be passed as the payload
     * to this object.
     *
     * @return the type
     */
    Class<PayloadT> getPayloadType();

    /**
     * Handles the given request from a client.
     *
     * @param responseReceiver the request object
     *
     */
    void handle(Client client, Request request, ConnectedClientService.ResponseReceiver responseReceiver);

}
