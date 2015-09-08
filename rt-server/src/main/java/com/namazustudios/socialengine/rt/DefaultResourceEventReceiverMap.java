package com.namazustudios.socialengine.rt;

import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the {@link ResourceEventReceiverMap}.
 *
 * Created by patricktwohig on 9/4/15.
 */
public class DefaultResourceEventReceiverMap implements ResourceEventReceiverMap {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultResourceEventReceiverMap.class);

    private final ReadWriteProtectedTreeMultimap<String, EventReceiverWrapper<?>> eventSubscribers =
              new ReadWriteProtectedTreeMultimap<>();

    @Override
    public <EventT> Subscription subscribe(final String key, final EventReceiver<EventT> eventReceiver) {

        final EventReceiverWrapper<EventT> wrapper = new EventReceiverWrapper<>(eventReceiver);

        eventSubscribers.write(new ReadWriteProtectedMultimap.CriticalSection<Void,
                String, EventReceiverWrapper<?>, SortedSetMultimap<String,EventReceiverWrapper<?>>>() {
            @Override
            public Void perform(SortedSetMultimap<String, EventReceiverWrapper<?>> eventReceivers) {
                eventReceivers.put(key, wrapper);
                LOG.debug("Registered event receiver {}", eventReceiver);
                return null;
            }
        });

        return new Subscription() {

            @Override
            public void release() {
                eventSubscribers.write(new ReadWriteProtectedMultimap.CriticalSection<Void, String,
                        EventReceiverWrapper<?>, SortedSetMultimap<String,EventReceiverWrapper<?>>>() {
                    @Override
                    public Void perform(SortedSetMultimap<String, EventReceiverWrapper<?>> eventReceivers) {
                        LOG.debug("Unregistered event receiver {}", eventReceiver);
                        eventReceivers.remove(key, wrapper);
                        return null;

                    }
                });
            }

        };

    }

    public <EventT> void post(final Path path, final EventT event, final String name) {
        eventSubscribers.read(new ReadWriteProtectedMultimap.CriticalSection<Void, String,
                EventReceiverWrapper<?>, SortedSetMultimap<String,EventReceiverWrapper<?>>>() {

            @Override
            public Void perform(SortedSetMultimap<String, EventReceiverWrapper<?>> eventReceivers) {
                final List<EventReceiverWrapper<?>> eventReceiverList = new ArrayList<>(eventReceivers.get(name));

                for (EventReceiver<?> eventReceiver : eventReceiverList) {
                    try {
                        final Object eventObject = eventReceiver.getEventType().cast(event);
                        final EventReceiver<Object> objectEventReceiver = (EventReceiver<Object>) eventReceiver;
                        objectEventReceiver.receive(path, name, eventObject);
                    } catch (ClassCastException ex) {
                        LOG.warn("Incompatible event type.", ex);
                    }
                }

                return null;

            }
        });
    }

}
