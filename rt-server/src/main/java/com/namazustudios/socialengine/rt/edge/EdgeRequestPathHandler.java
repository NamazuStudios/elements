package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.RequestHeader;
import com.namazustudios.socialengine.rt.ResponseHeader;
import com.namazustudios.socialengine.rt.ResponseReceiver;

/**
 * An instance of EdgeRequestPathHandler is responsible for producing {@link ResponseHeader} objects
 * from instances of {@link RequestHeader}.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface EdgeRequestPathHandler {

    /**
     * Gets the {@link Class} which can be passed as the payload
     * to this object.
     *
     * @return the type
     */
    Class<?> getPayloadType();

    /**
     * Handles the given request from a edgeClientSession.
     *
     * In the event the {@link Request#getPayload()} method returns an object that is not compatible
     * with this instance and exception can be raised.  Acceptability can be determined by
     * the usage of {@link Class#isAssignableFrom(Class)}.
     *
     * @param edgeClientSession the {@link EdgeClientSession} making the request.
     * @param request the {@link Request} object
     * @param responseReceiver the request object
     *
     * @throws {@link InvalidDataException} if the return of the {@link Request#getPayload()} method is not suitable.
     *
     */
    void handle(EdgeClientSession edgeClientSession, Request request, ResponseReceiver responseReceiver);

}
