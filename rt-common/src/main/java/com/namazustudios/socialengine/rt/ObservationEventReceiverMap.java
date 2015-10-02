package com.namazustudios.socialengine.rt;

/**
 * Instances of {@link ObservationEventReceiverMap} manages instances of {@link EventReceiver} in such
 * a way that the events are loosely coupled with the resource which sources them.
 *
 * Generally speaking it is responsiblity of the user of this class to observe for changes in paths
 * and update its handlers accordingly.
 *
 * Created by patricktwohig on 9/5/15.
 */
public interface ObservationEventReceiverMap {

    /**
     * Creates a {@link Subscription} object to the given event path and name with the given
     * receiver.
     *
     * @param <EventT>
     * @param path the path (may be wildcard)
     * @param name the event name
     * @param eventReceiver the {@link EventReceiver} to dispatch the event
     * @return
     */
    <EventT> Observation subscribe(Path path, String name, EventReceiver<EventT> eventReceiver);

    /**
     * Gets all {@link EventReceiver} instances matching the given path and name.
     *
     * @param path path
     * @param name name
     * @return an {@link Iterable} of {@link EventReceiver} instances
     */
    Iterable<? extends EventReceiver<?>> getEventReceivers(Path path, String name);

    /**
     * Dispatches the given {@link Event} of the given type.
     *
     * @param event the {@link Event}
     */
    void dispatch(Event event);

}
