package com.namazustudios.socialengine.rt;

import com.google.common.collect.SortedSetMultimap;
import com.namazustudios.socialengine.rt.event.EventModel;

/**
 * Created by patricktwohig on 9/4/15.
 */
public interface ResourceEventSubscriberMap {

    /**
     * Subscribes to {@link Event}s using the given {@link EventReceiver}.
     *
     * @praam key the key of the event
     * @param eventReceiver the event receiver instance
     *
     */
    <EventT> Subscription subscribe(String name, EventReceiver<EventT> eventReceiver);

    /**
     * Posts the given event to all of this objects' {@link EventReceiver} instances.  This ensures
     * that the event is checked and delivered to the appropriate handlers.
     *
     * This method will throw an execpetion if the type is not annotated with {@link EventModel}
     *
     * @param <EventT>
     * @param event the event itself
     * @param name the name of the event
     */
    <EventT> void post(final Path path, final EventT event, final String name);

}
