package com.namazustudios.socialengine.rt;

/**
 * Tracks {@link EventReceiver} instances for the specific local {@link Resource} instance.
 *
 * Created by patricktwohig on 9/4/15.
 */
public interface ResourceEventReceiverMap {

    /**
     * Subscribes a wildcard receiver to the {@link Resource}.  This will observe events with
     * any name.
     *
     * This method must be thread safe.
     *
     * @param objectEventReceiver
     * @return the {@link Observation} instance
     */
    Observation observe(EventReceiver<Object> objectEventReceiver);

    /**
     * Subscribes to {@link Event}s using the given {@link EventReceiver}.
     *
     * @praam key the key of the event
     * @param eventReceiver the event receiver instance
     *
     */
    <EventT> Observation subscribe(String name, EventReceiver<EventT> eventReceiver);

    /**
     * Posts the given event to all of this objects' {@link EventReceiver} instances.  This ensures
     * that the event is checked and delivered to the appropriate handlers.
     *
     */
    <PayloadT> void post(final Event event);

}
