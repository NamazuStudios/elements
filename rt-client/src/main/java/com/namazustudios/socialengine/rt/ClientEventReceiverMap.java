package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 9/5/15.
 */
public interface ClientEventReceiverMap {

    /**
     * Creates a {@link Subscription} object to the given event path and name with the given
     * receiver.
     *
     * @param path the path (may be wildcard)
     * @param name the event name
     * @param eventReceiver the {@link EventReceiver} to receive the event
     * @param <EventT>
     * @return
     */
    <EventT> Subscription subscribe(Path path, String name, EventReceiver<EventT> eventReceiver);

    /**
     * Gets all {@link EventReceiver} instances matching the given path and name.
     *
     * @param path path
     * @param name name
     * @return an {@link Iterable} of {@link EventReceiver} instances
     */
    Iterable<? extends EventReceiver<?>> getEventReceivers(Path path, String name);

}
