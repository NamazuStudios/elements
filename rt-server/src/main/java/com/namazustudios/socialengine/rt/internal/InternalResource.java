package com.namazustudios.socialengine.rt.internal;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.edge.EdgeRequestPathHandler;

/**
 * Created by patricktwohig on 8/23/15.
 */
public interface InternalResource extends Resource {

    /**
     * Gets the RequestHandler for the method.
     *
     * @param method the method name
     * @return the handler for the given method, never null
     *
     * @throws {@link NotFoundException} if the method cannot be found.
     */
    InternalRequestPathHandler<?> getHandler(final String method);

}
