package com.namazustudios.socialengine.rt;

/**
 * Used to specify the location of the event as a path.
 */
public interface EventObservationPathBuilder<ObservationT> {

    /**
     * Completes the subscription process with the path.  Events from the {@link Resource} will
     * begin receiving the events.
     *
     * @param path the path (as a string)
     * @return
     */
    ObservationT atPath(String path);

    /**
     * Completes the subscription process with the path.  Events from the {@link Resource} will
     * begin receiving the events.
     *
     * @param path the path as an object
     * @return the {@link Subscription} instance
     */
    ObservationT atPath(Path path);

}
