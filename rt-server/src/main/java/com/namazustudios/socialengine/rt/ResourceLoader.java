package com.namazustudios.socialengine.rt;

/**
 * Loads insthaces of {@link Resource}.
 */
public interface ResourceLoader extends AutoCloseable {

    /**
     * Closes the {@link ResourceLoader} and cleaning up any resources.  Any open {@link Resource}
     * instances may be closed, but this is not a guarantee.  All resources open <b>should</b> be closed
     * before closing this {@link ResourceLoader}.  Using {@link Resource}s after closing this instance, or
     * closing this instance while resources are open should be considered undefined behavior.
     *
     * Invoking this method twice on the same object should also be considered undefined behavior.
     */
    @Override
    void close();

}
