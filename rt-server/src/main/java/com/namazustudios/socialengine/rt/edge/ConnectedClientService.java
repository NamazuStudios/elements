package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.Client;
import com.namazustudios.socialengine.rt.EventReceiver;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.edge.EdgeResponseReceiver;

/**
 * Represents the currently connected clients and provides a means to
 * dispatch messages to clients.
 *
 * Created by patricktwohig on 7/26/15.
 */
public interface ConnectedClientService {

    /**
     * Gets the {@link EdgeResponseReceiver} instance which can be used to receive messages
     * of the given type.
     *
     * @param client the client
     * @param request the request
     * @return the {@link EdgeResponseReceiver} which will receive the response
     *
     * @throws {@link NotFoundException} if no active connection for the client can be found
     */
    EdgeResponseReceiver getResponseReceiver(Client client, Request request);

}
