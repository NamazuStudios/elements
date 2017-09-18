package com.namazustudios.socialengine.rt.handler;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.RequestHeader;
import com.namazustudios.socialengine.rt.ResponseHeader;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import com.namazustudios.socialengine.rt.exception.BadRequestException;

/**
 * An instance of ClientRequestHandler is responsible for producing {@link ResponseHeader} objects
 * from instances of {@link RequestHeader}.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface ClientRequestHandler {

    /**
     * Gets the {@link Class} which can be passed as the payload
     * to this object.
     *
     * @return the type
     */
    Class<?> getPayloadType();

    /**
     * Handles the given request from a handlerClientSession.
     *
     * In the event the {@link Request#getPayload()} method returns an object that is not compatible
     * with this instance and exception can be raised.  Acceptability can be determined by
     * the usage of {@link Class#isAssignableFrom(Class)}.
     *
     * @param handlerClientSession the {@link HandlerClientSession} making the request.
     * @param request the {@link Request} object
     * @param responseReceiver the request object
     *
     * @throws {@link BadRequestException} if the return of the {@link Request#getPayload()} method is not suitable.
     *
     */
    void handle(HandlerClientSession handlerClientSession, Request request, ResponseReceiver responseReceiver);

}
