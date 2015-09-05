package com.namazustudios.socialengine.rt;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of {@link ReadWriteProtectedMultimap} backed by an instance of {@link TreeMultimap}
 *
 * Created by patricktwohig on 9/5/15.
 */
public class ReadWriteProtectedTreeMultimap<KeyT extends Comparable<KeyT>,
                                            ValueT extends Comparable<ValueT>>
        implements ReadWriteProtectedMultimap<KeyT, ValueT, SortedSetMultimap<KeyT, ValueT>> {

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final SortedSetMultimap<KeyT, ValueT> eventReceivers = TreeMultimap.create();

    private final SortedSetMultimap<KeyT, ValueT> eventReceiversUnmodifiableView =
            Multimaps.unmodifiableSortedSetMultimap(eventReceivers);

    @Override
    public <ReturnT> ReturnT read(CriticalSection<ReturnT, KeyT, ValueT, SortedSetMultimap<KeyT, ValueT>> criticalSection) {
        final Lock readLock = readWriteLock.readLock();

        readLock.lock();

        try {
            return criticalSection.perform(eventReceiversUnmodifiableView);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public <ReturnT> ReturnT write(CriticalSection<ReturnT, KeyT, ValueT, SortedSetMultimap<KeyT, ValueT>> criticalSection) {
        final Lock writeLock = readWriteLock.writeLock();

        writeLock.lock();

        try {
            return criticalSection.perform(eventReceivers);
        } finally {
            writeLock.unlock();
        }
    }

}
