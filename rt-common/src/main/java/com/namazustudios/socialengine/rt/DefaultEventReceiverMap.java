package com.namazustudios.socialengine.rt;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by patricktwohig on 9/4/15.
 */
public class DefaultEventReceiverMap<KeyT> implements EventReceiverMap<KeyT> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultEventReceiverMap.class);

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final SetMultimap<KeyT, EventReceiver<?>> eventReceivers = LinkedHashMultimap.create();

    private final SetMultimap<KeyT, EventReceiver<?>> eventReceiversUnmodifiableView = Multimaps.unmodifiableSetMultimap(eventReceivers);

    @Override
    public <EventT> Subscription subscribe(final KeyT key, final EventReceiver<EventT> eventReceiver) {

        final EventReceiver<EventT> wrapper = new EventReceiverWrapper<>(eventReceiver);

        write(new CriticalSection<KeyT, Void>() {
            @Override
            public Void perform(SetMultimap<KeyT, EventReceiver<?>> eventReceivers) {
                eventReceivers.put(key, eventReceiver);
                LOG.debug("Registered event receiver {}", eventReceiver);
                return null;
            }
        });

        return new Subscription() {

            @Override
            public void release() {
                write(new CriticalSection<KeyT, Void>() {
                    @Override
                    public Void perform(SetMultimap<KeyT, EventReceiver<?>> eventReceivers) {
                        LOG.debug("Unregistered event receiver {}", eventReceiver);
                        eventReceivers.remove(key, wrapper);
                        return null;
                    }
                });
            }

        };

    }

    @Override
    public <ReturnT> ReturnT read(final CriticalSection<KeyT, ReturnT> criticalSection) {

        final Lock readLock = readWriteLock.readLock();

        readLock.lock();

        try {
            return criticalSection.perform(eventReceiversUnmodifiableView);
        } finally {
            readLock.unlock();
        }

    }

    @Override
    public <ReturnT> ReturnT write(final CriticalSection<KeyT, ReturnT> criticalSection) {

        final Lock writeLock = readWriteLock.writeLock();

        writeLock.lock();

        try {
            return criticalSection.perform(eventReceivers);
        } finally {
            writeLock.unlock();
        }

    }

}
