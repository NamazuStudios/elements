package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResponseReceiver;

/**
 * Represents the currently connected clients and provides a means to
 * dispatch messages to clients.
 *
 * Created by patricktwohig on 7/26/15.
 */
public interface ConnectedEdgeClientService {

    /**
     * Gets the {@link ResponseReceiver} instance which can be used to receive messages
     * of the given type.
     *
     * @param edgeClientSession the edgeClientSession
     * @param request the request
     * @return the {@link ResponseReceiver} which will receive the response
     *
     * @throws {@link NotFoundException} if no active connection for the edgeClientSession can be found
     */
    ResponseReceiver getResponseReceiver(EdgeClientSession edgeClientSession, Request request);

}
