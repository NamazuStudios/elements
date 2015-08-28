package com.namazustudios.socialengine.rt.internal;

import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResponseReceiver;

/**
 * Used by {@link InternalResource} instances to handle {@link Request}s.
 *
 * Created by patricktwohig on 8/23/15.
 */
public interface InternalRequestPathHandler {

    /**
     * Gets the {@link Class} which can be passed as the payload
     * to this object.
     *
     * @return the type
     */
    Class<?> getPayloadType();

    /**
     * Handles the given request from a edgeClient.
     *
     * In the event the {@link Request#getPayload()} method returns an object that is not compatible
     * with this instance and exception can be raised.  Acceptability can be determined by
     * the usage of {@link Class#isAssignableFrom(Class)}.
     *
     * @param request the {@link Request} object
     * @param responseReceiver the request object
     *
     * @throws {@link InvalidDataException} if the return of the {@link Request#getPayload()} method is not suitab.e
     *
     */
    void handle(Request request, ResponseReceiver responseReceiver);

}
