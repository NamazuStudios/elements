package com.namazustudios.socialengine.rt;

/**
 * Used to specify the location of the event as a path.
 */
public interface PathBuilder<NextT> {

    /**
     * Completes the subscription process with the path.  Events from the {@link Resource} will
     * begin receiving the events.
     *
     * @param path the path (as a string)
     * @return
     */
    NextT atPath(String path);

    /**
     * Completes the subscription process with the path.  Events from the {@link Resource} will
     * begin receiving the events.
     *
     * @param path the path as an object
     * @return the {@link NextT} instance
     */
    NextT atPath(Path path);

}
