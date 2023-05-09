package dev.getelements.elements.test;

import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.remote.Instance;

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
