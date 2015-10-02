package com.namazustudios.socialengine.rt;

import com.google.common.collect.Lists;
import com.google.common.collect.SortedSetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedSet;

/**
 * Created by patricktwohig on 9/5/15.
 */
public class DefaultObservationEventReceiverMap implements ObservationEventReceiverMap {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultObservationEventReceiverMap.class);

    private ReadWriteProtectedSortedSetMultimap<Path, EventReceiverWrapper<?>> receiverMap =
            new ReadWriteProtectedSortedSetMultimap<>();

    @Override
    public <EventT> Observation subscribe(final Path path, final String name, final EventReceiver<EventT> eventReceiver) {

        final Path absolutePath = new Path(name).append(path);
        final EventReceiverWrapper<EventT> eventReceiverWrapper = new EventReceiverWrapper<>(eventReceiver);

        receiverMap.write(new ReadWriteProtectedObject.CriticalSection<Void, SortedSetMultimap<Path,EventReceiverWrapper<?>>>() {
            @Override
            public Void perform(SortedSetMultimap<Path, EventReceiverWrapper<?>> protectedObject) {
                protectedObject.put(absolutePath, eventReceiverWrapper);
                return null;
            }
        });

        return new Observation() {
            @Override
            public void release() {
                receiverMap.write(new ReadWriteProtectedObject.CriticalSection<Void, SortedSetMultimap<Path,EventReceiverWrapper<?>>>() {
                    @Override
                    public Void perform(final SortedSetMultimap<Path, EventReceiverWrapper<?>> eventReceivers) {
                        eventReceivers.remove(absolutePath, eventReceiverWrapper);
                        return null;
                    }
                });
            }
        };

    }

    @Override
    public Iterable<? extends EventReceiver<?>> getEventReceivers(final Path path, final String name) {
        final Path absolutePath = new Path(name).append(path);
        final Iterable<EventReceiverWrapper<?>> eventReceiverWrappers;

        eventReceiverWrappers = receiverMap.read(new ReadWriteProtectedObject.CriticalSection<Iterable<EventReceiverWrapper<?>>, SortedSetMultimap<Path, EventReceiverWrapper<?>>>() {
            @Override
            public Iterable<EventReceiverWrapper<?>> perform(SortedSetMultimap<Path, EventReceiverWrapper<?>> protectedObject) {
                final SortedSet<EventReceiverWrapper<?>> eventReceiverWrappers = protectedObject.get(absolutePath);
                return Lists.newArrayList(eventReceiverWrappers);
            }
        });

        return eventReceiverWrappers;

    }

    @Override
    public void dispatch(final Event event, final Class<?> eventType) {

        final Path path = new Path(event.getEventHeader().getPath());
        final String name = event.getEventHeader().getName();

        for(EventReceiver<?> eventReceiver : getEventReceivers(path, name)) {
            try {
                final Object payload = eventReceiver.getEventType().cast(event.getPayload());
                final EventReceiver<Object> objectEventReceiver = (EventReceiver<Object>)eventReceiver;
                objectEventReceiver.receive(path, name, payload);
            } catch (ClassCastException ex) {
                LOG.error("Caught exception trying to process event {} with receiver {}.", event, eventReceiver);
            }
        }

    }

}
