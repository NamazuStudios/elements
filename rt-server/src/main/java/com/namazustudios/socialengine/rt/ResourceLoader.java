package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.ModuleNotFoundException;

/**
 * Loads instances of {@link Resource} based on {@link Path} instances.  Typically instances of this
 * work in conjunction with a {@link AssetLoader} to load the raw bytes from storage and then into
 * memory.
 */
public interface ResourceLoader extends AutoCloseable {

    /**
     * Loads the {@Link Resource} specified by the supplied module name.  The supplied module name is specific to
     * the particular implementation of the {@link ResourceLoader}, but should be a unique identifier specifying the
     *
     * @param moduleName the module name
     * @return the {@link Resource} instance, never null
     *
     * @throws {@link ModuleNotFoundException} if the source for the {@link Resource} cannot be found.
     */
    Resource load(final String moduleName) throws ModuleNotFoundException;

    /**
     * Closes the {@link ResourceLoader} and cleaning up any resources.  Any open {@link Resource}
     * instances may be closed, but this is not a guarantee.  All resources open <b>should</b> be closed
     * before closing this {@link ResourceLoader}.  Using {@link Resource}s after closing this instance, or
     * closing this instance while resources are open should be considered undefined behavior.
     *
     * This also closes the underlying {@link AssetLoader} which may be associated with this {@link ManifestLoader}.  If
     * pooling or sharing resources of {@link AssetLoader} instances is desired, then something such as
     * {@link AssetLoader#getReferenceCountedView()} should be used.
     *
     * Invoking this method twice on the same object should also be considered undefined behavior.
     */
    @Override
    void close();

}
