package com.namazustudios.socialengine.rt;

/**
 * An instance of PathHandler is responsible for producing {@link Response} objects
 * from instances of {@link Request}.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface PathHandler {

    /**
     * Handles the given request from a client.
     *
     * @param client the client handle, this is opaque and should only be used
     * @param receiver the request object
     *
     */
    <PayloadT> void handle(Client client, Receiver<Response, PayloadT> receiver);

}
