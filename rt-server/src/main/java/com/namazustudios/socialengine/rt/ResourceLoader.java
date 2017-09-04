package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;

/**
 * Loads instances of {@link Resource} based on {@link Path} instances.  Typically instances of this
 * work in conjunction with a {@link AssetLoader} to load the raw bytes from storage and then into
 * memory.
 */
public interface ResourceLoader extends AutoCloseable {

    /**
     * Loads the {@Link Resource} specified by the supplied {@link Path} instance.  The supplied {@link Path}
     * specifies the source of the underlying {@link Resource}.  A new instance of the {@link Resource} will
     * be be instantiated and returned.
     *
     * @param path the {@link Path} to the {@link Resource}'s source
     * @return the {@link Resource} instance, never null
     *
     * @throws {@link ResourceNotFoundException} if the source for the {@link Resource} cannot be found.
     */
    Resource load(Path path) throws ResourceNotFoundException;

    /**
     * Performs the same operation as {@link #load(Path)}, only accepts a {@link String} path, which is
     * then converted to a {@link Path}.
     *
     * @param pathString the {@link Path} string {@see {@link Path#Path(String)}}
     * @return the {@link Resource}
     */
    default Resource load(final String pathString) {
        final Path path = new Path(pathString);
        return load(path);
    }

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
