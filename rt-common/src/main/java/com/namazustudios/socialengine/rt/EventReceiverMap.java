package com.namazustudios.socialengine.rt;

import com.google.common.collect.SetMultimap;

/**
 * Created by patricktwohig on 9/4/15.
 */
public interface EventReceiverMap<KeyT> {
    /**
     * Subscribes to {@link Event}s using the given {@link EventReceiver}.
     *
     * @praam key the key of the event
     * @param eventReceiver the event receiver instance
     *
     * @param <EventT>
     *
     */
    <EventT> Subscription subscribe(KeyT key, EventReceiver<EventT> eventReceiver);

    /**
     * Exposes the underlying map for reading.
     *
     * @param criticalSection the {@link CriticalSection} used to read the map.
     */
    <ReturnT> ReturnT read(CriticalSection<KeyT, ReturnT> criticalSection);

    /**
     * Exposes the underlying map for writing.
     *
     * @param criticalSection the {@link CriticalSection} used to read the map.
     */
    <ReturnT> ReturnT write(CriticalSection<KeyT, ReturnT> criticalSection);

    /**
     * Simple callback for manipulating the underlying collection.
     *
     * @param <KeyT>
     */
    interface CriticalSection<KeyT, ReturnT> {

        /**
         * Called to perform the critical operation.
         *
         * @param eventReceivers the {@link SetMultimap} of event receivers to manipulate
         *
         * @return the custom return type
         */
        ReturnT perform(SetMultimap<KeyT, EventReceiver<?>> eventReceivers);

    }
}
