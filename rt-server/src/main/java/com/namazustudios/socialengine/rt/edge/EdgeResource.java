package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.*;

/**
 *
 * An instance of {@link EdgeResource} exists to talk to the outside world.  That is,
 * it is responsible for handling {@link Request} instances from {@link EdgeClient}s.
 *
 * {@link EdgeResource} instances should live the life of the server, and should not
 * be moved.  Rather the ege resources shoudl be responsible for manipulating the
 * state of internal resources.
 *
 */
public interface EdgeResource extends Resource {

    /**
     * Gets the path at which the script is to be boostrapped.
     */
    String getBootstrapPath();

    /**
     * Gets the RequestHandler for the method.
     *
     * @param method the method name
     * @return the handler for the given method, never null
     *
     * @throws {@link NotFoundException} if the method cannot be found.
     */
    EdgeRequestPathHandler getHandler(final String method);

}
