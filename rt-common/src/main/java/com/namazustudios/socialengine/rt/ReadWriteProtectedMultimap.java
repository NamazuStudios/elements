package com.namazustudios.socialengine.rt;

import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.SortedSetMultimap;

/**
 * Created by patricktwohig on 9/5/15.
 */
public interface ReadWriteProtectedMultimap<KeyT, ValueT, MultimapT extends Multimap<KeyT, ValueT>> {

    /**
     * Exposes the underlying map for reading.
     *
     * @param criticalSection the {@link CriticalSection} used to read the map.
     */
    <ReturnT> ReturnT read(CriticalSection<ReturnT, KeyT, ValueT, MultimapT> criticalSection);

    /**
     * Exposes the underlying map for writing.
     *
     * @param criticalSection the {@link CriticalSection} used to read the map.
     */
    <ReturnT> ReturnT write(CriticalSection<ReturnT, KeyT, ValueT, MultimapT> criticalSection);

    /**
     * Simple callback for manipulating the underlying collection.
     *
     * @param <ReturnT> the return type.
     */
    interface CriticalSection<ReturnT, KeyT, ValueT, MultimapT extends Multimap<KeyT, ValueT>> {

        /**
         * Called to perform the critical operation.
         *
         * @param eventReceivers the {@link SetMultimap} of event receivers to manipulate
         *
         * @return the custom return type
         */
        ReturnT perform(MultimapT eventReceivers);

    }

}
