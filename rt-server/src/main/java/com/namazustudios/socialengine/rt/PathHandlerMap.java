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
public interface PathHandlerMap<ClientT> {

    /**
     * Gets the request handler for the given path.  The path is
     * user defined and is ultimately the destination for a {@link Request}
     *
     * @param path the path
     * @param <PayloadT> the payload type.
     * @return the PathHandler to handle the path
     *
     * @throws {@link NotFoundException} if the given handler cannot be found
     * @throws {@link InvalidDataException} } if the handler is found, but does not match the payload
     */
    <PayloadT> PathHandler<ClientT> getPathHandler(final String path, Class<PayloadT> payloadTClass);

}
