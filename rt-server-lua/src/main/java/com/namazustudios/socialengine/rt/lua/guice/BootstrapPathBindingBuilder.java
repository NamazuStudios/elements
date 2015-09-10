package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.binder.ScopedBindingBuilder;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.edge.EdgeResource;

/**
 * Used specifically to sepcify the bootstrap path where {@link EdgeResource} instances
 * will be bound in the associated {@link ResourceService}.
 *
 * Created by patricktwohig on 9/9/15.
 */
public interface BootstrapPathBindingBuilder {

    /**
     * Binds the {@link EdgeResource} to the linek {@link Path}.
     *
     * @param path the path
     * @return the {@link ScopedBindingBuilder}
     */
    ScriptFileBindingBuilder onBootstrapPath(final Path path);

    /**
     * Binds the {@link EdgeResource} to the link {@link Path}, represented
     * as a string.
     *
     * @param path the path
     * @return the {@link ScopedBindingBuilder}
     */
    ScriptFileBindingBuilder onBootstrapPath(final String path);

}
