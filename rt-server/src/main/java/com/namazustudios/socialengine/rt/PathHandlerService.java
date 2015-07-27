package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;

/**
 * The core server to the RT component.  Note that the server itself is
 * just responsible for dispatching requests.  It actually has no logic
 * at all for handling network code.  Other downstream projects handle
 * that process.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface PathHandlerService {

    /**
     * Adds a path handler for hte given Payload type.
     *
     * @param handler the handler
     * @param method the method to handle at the path
     * @param payloadTClass the payload type
     * @param <PayloadT> the payload type
     */
    <PayloadT> void addPathHandler(PathHandler handler, String method, Class<PayloadT> payloadTClass);

    /**
     * Gets the request handler for the given path.  The path is
     * user defined and is ultimately the destination for a {@link RequestHeader}
     *
     * @param requestHeader the request header.
     * @param <PayloadT> the payload type.
     * @return the PathHandler to handle the path
     *
     * @throws {@link NotFoundException} if the given handler cannot be found
     * @throws {@link InvalidDataException} if the handler is found, but does not match the payload type
     */
    <PayloadT> PathHandler<PayloadT> getPathHandler(RequestHeader requestHeader);

}
