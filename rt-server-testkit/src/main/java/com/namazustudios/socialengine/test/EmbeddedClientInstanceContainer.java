package com.namazustudios.socialengine.test;

import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.remote.Instance;

/**
 * A container for a client {@link Instance}.
 */
public interface EmbeddedClientInstanceContainer extends EmbeddedInstanceContainer {

    /**
     * Gets the {@link Context.Factory} used to resolve client {@link Context} instances.
     *
     * @return the {@link Context.Factory}
     */
    Context.Factory getContextFactory();

}
