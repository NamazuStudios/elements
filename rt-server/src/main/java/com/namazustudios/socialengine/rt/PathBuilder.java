package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.Subscription;

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
     * @return the {@link Subscription} instance
     */
    NextT atPath(Path path);

}
