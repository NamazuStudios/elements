package com.namazustudios.socialengine.rt;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of {@link ReadWriteProtectedObject} backed by an instance of {@link TreeMultimap}
 *
 * Created by patricktwohig on 9/5/15.
 */
public class ReadWriteProtectedSortedSetMultimap<KeyT extends Comparable<KeyT>,
                                          ValueT extends Comparable<ValueT>>
        extends AbstractReadWriteProtectedObject<SortedSetMultimap<KeyT,ValueT>> {

    private final SortedSetMultimap<KeyT, ValueT> eventReceivers = TreeMultimap.create();

    private final SortedSetMultimap<KeyT, ValueT> eventReceiversUnmodifiableView =
            Multimaps.unmodifiableSortedSetMultimap(eventReceivers);

    @Override
    protected SortedSetMultimap<KeyT, ValueT> mutableView() {
        return eventReceivers;
    }

    @Override
    protected SortedSetMultimap<KeyT, ValueT> immutableView() {
        return eventReceiversUnmodifiableView;
    }

}
