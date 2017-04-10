package com.namazustudios.socialengine.rt;

import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The simple implementation of {@link EventService} which uses an in-memory mapping and a read-write
 * locking strategy to ensure concurrency control.
 *
 * When dispatching events and finding instances of {@link EventReceiver}
 *
 * Created by patricktwohig on 4/1/16.
 */
public class SimpleEventService implements EventService {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleEventService.class);

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private SortedSetMultimap<ObservationKey, EventReceiverWrapper<?>> internalMapping = TreeMultimap.create();

    @Override
    public void post(final Event event) {

        final Path path = new Path(event.getEventHeader().getPath());
        final Iterable<? extends EventReceiver<?>> receivers = getEventReceivers(path, event.getEventHeader().getName());

        for (EventReceiver<?> wrapper : receivers) {
            try {
                final Object o = wrapper.getEventType().cast(event.getPayload());
                wrapper.receive(event);
            } catch (ClassCastException ex) {
                LOG.debug("Type mismatch for wrapper {}", wrapper, event);
            }
        }

    }

    @Override
    public <PayloadT> Observation observe(final Path path, final String name,
                                          final EventReceiver<PayloadT> receiver) {

        final ObservationKey key = new ObservationKey(path, name);
        final EventReceiverWrapper<PayloadT> receiverWrapper = new EventReceiverWrapper<>(receiver);

        final Observation observation = () -> {

            readWriteLock.writeLock().lock();

            try {
                internalMapping.remove(key, receiver);
            } finally {
                readWriteLock.writeLock().unlock();
            }

        };

        readWriteLock.writeLock().lock();

        try {
            internalMapping.put(key, receiverWrapper);
            return observation;
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    @Override
    public Iterable<? extends EventReceiver<?>> getEventReceivers(final Path path, final String name) {

        final ObservationKey key = new ObservationKey(path, name);

        final List<EventReceiverWrapper<?>> receivers;

        readWriteLock.readLock().lock();

        try {
            receivers = new ArrayList<>(internalMapping.get(key));
        } finally {
            readWriteLock.readLock().unlock();
        }

        return receivers;

    }

    private class ObservationKey implements Comparable<ObservationKey> {

        private final Path path;

        private final String name;

        public ObservationKey(Path path, String name) {
            this.path = path;
            this.name = name;
        }

        @Override
        public int compareTo(ObservationKey o) {
            int cmp = path.compareTo(o.path);
            return cmp == 0 ? name.compareTo(o.name) : cmp;
        }

    }

}
