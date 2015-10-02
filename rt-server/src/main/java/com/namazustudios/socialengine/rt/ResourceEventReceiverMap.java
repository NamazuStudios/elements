package com.namazustudios.socialengine.rt;

import com.google.common.collect.SortedSetMultimap;
import com.namazustudios.socialengine.rt.event.EventModel;

/**
 * Tracks {@link EventReceiver} instances for the specific local {@link Resource} instance.
 *
 * Created by patricktwohig on 9/4/15.
 */
public interface ResourceEventReceiverMap {

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
     * @param <EventT>
     * @param event the event itself
     * @param name the name of the event
     */
    <EventT> void post(final Path path, final EventT event, final String name);

}
