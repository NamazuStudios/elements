package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.*;

/**
 * A Resource is essentially a type that is capable primarly of both
 * receiving {@link Request} instances to produce {@link Response}
 * instances.
 *
 * Additionally, a Resource can be the source of {@link EventHeader} objects
 * which can be transmitted from the server to the client.
 *
 * Typically instances of Resource have their own scope, and
 * communicate primarily with other Resources through either events
 * or requests.
 *
 * Once a resource is no longer needed, it is necessary to destroy the
 * resource using the {@link AutoCloseable#close()} method.
 *
 * Created by patricktwohig on 8/8/15.
 */
public interface EdgeResource extends Resource {

    /**
     * Gets the RequestHandler for the method.
     *
     * @param method the method name
     * @return the handler for the given method, never null
     *
     * @throws {@link NotFoundException} if the method cannot be found.
     */
    EdgeRequestPathHandler<?> getHandler(final String method);

}
