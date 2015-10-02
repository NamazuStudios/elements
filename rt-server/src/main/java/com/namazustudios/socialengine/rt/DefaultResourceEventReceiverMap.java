package com.namazustudios.socialengine.rt;

import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Default implementation of the {@link ResourceEventReceiverMap}.
 *
 * Created by patricktwohig on 9/4/15.
 */
public class DefaultResourceEventReceiverMap implements ResourceEventReceiverMap {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultResourceEventReceiverMap.class);

    private final ConcurrentSkipListSet<EventReceiver<Object>> observers = new ConcurrentSkipListSet<>();

    private final ReadWriteProtectedSortedSetMultimap<String, EventReceiverWrapper<?>> eventSubscribers =
              new ReadWriteProtectedSortedSetMultimap<>();

    @Override
    public Observation observe(final EventReceiver<Object> objectEventReceiver) {

        final EventReceiverWrapper<Object> eventReceiverWrapper = new EventReceiverWrapper<>(objectEventReceiver);
        observers.add(eventReceiverWrapper);

        return new Observation() {
            @Override
            public void release() {
                observers.remove(eventReceiverWrapper);
            }
        };

    }

    @Override
    public <EventT> Observation subscribe(final String name, final EventReceiver<EventT> eventReceiver) {

        final EventReceiverWrapper<EventT> wrapper = new EventReceiverWrapper<>(eventReceiver);

        eventSubscribers.write(new ReadWriteProtectedObject.CriticalSection<Void, SortedSetMultimap<String,EventReceiverWrapper<?>>>() {
            @Override
            public Void perform(SortedSetMultimap<String, EventReceiverWrapper<?>> protectedObject) {
                protectedObject.put(name, wrapper);
                LOG.debug("Registered event receiver {}", eventReceiver);
                return null;
            }
        });

        return new Observation() {

            @Override
            public void release() {
                eventSubscribers.write(new ReadWriteProtectedObject.CriticalSection<Void, SortedSetMultimap<String,EventReceiverWrapper<?>>>() {
                    @Override
                    public Void perform(SortedSetMultimap<String, EventReceiverWrapper<?>> eventReceivers) {
                        LOG.debug("Unregistered event receiver {}", eventReceiver);
                        eventReceivers.remove(name, wrapper);
                        return null;

                    }
                });
            }

        };

    }

    public <PayloadT> void post(final Event event) {

        final String name = event.getEventHeader().getName();
        final Path path = new Path(event.getEventHeader().getPath());
        final Object payload = event.getPayload();

        eventSubscribers.read(new ReadWriteProtectedObject.CriticalSection<Void, SortedSetMultimap<String,EventReceiverWrapper<?>>>() {

            @Override
            public Void perform(SortedSetMultimap<String, EventReceiverWrapper<?>> eventReceivers) {
                final List<EventReceiverWrapper<?>> eventReceiverList = new ArrayList<>(eventReceivers.get(name));

                for (EventReceiver<?> eventReceiver : eventReceiverList) {
                    try {
                        final Object eventObject = eventReceiver.getEventType().cast(payload);
                        final EventReceiver<Object> objectEventReceiver = (EventReceiver<Object>) eventReceiver;
                        objectEventReceiver.receive(event);
                    } catch (ClassCastException ex) {
                        LOG.warn("Incompatible event type.", ex);
                    }
                }

                return null;

            }
        });

        for (final EventReceiver<Object> observerEventReceiver : Lists.newArrayList(observers)) {
            observerEventReceiver.receive(event);
        }

    }

}
