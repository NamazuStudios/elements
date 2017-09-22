package com.namazustudios.socialengine.rt;

/**
 * Represents the connection to the underlying cluster of {@link Resource}, {@link ResourceLoader}
 */
@Proxyable
public interface Context {

    /**
     * Shuts down this {@link Context} and disconnecting this {@link Context}.  The default implementation simply
     * defers all work to the managed services.
     */
    void shutdown();

}
