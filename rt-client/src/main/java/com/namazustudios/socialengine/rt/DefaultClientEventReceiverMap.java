package com.namazustudios.socialengine.rt;

import com.google.common.collect.Lists;
import com.google.common.collect.SortedSetMultimap;

import java.util.SortedSet;

/**
 * Created by patricktwohig on 9/5/15.
 */
public class DefaultClientEventReceiverMap implements ClientEventReceiverMap {

    private ReadWriteProtectedTreeMultimap<EventSubscriptionTuple, EventReceiverWrapper<?>> receiverMap =
            new ReadWriteProtectedTreeMultimap<>();

    @Override
    public <EventT> Subscription subscribe(Path path, String name, final EventReceiver<EventT> eventReceiver) {

        final EventSubscriptionTuple eventSubscriptionTuple = new EventSubscriptionTuple(name, path);
        final EventReceiverWrapper<EventT> eventReceiverWrapper = new EventReceiverWrapper<EventT>(eventReceiver);

        receiverMap.write(new ReadWriteProtectedMultimap.CriticalSection<
                Void,
                EventSubscriptionTuple,
                EventReceiverWrapper<?>,
                SortedSetMultimap<EventSubscriptionTuple,EventReceiverWrapper<?>>>() {

            @Override
            public Void perform(SortedSetMultimap<EventSubscriptionTuple, EventReceiverWrapper<?>> eventReceivers) {
                eventReceivers.put(eventSubscriptionTuple, eventReceiverWrapper);
                return null;
            }

        });

        return new Subscription() {
            @Override
            public void release() {
                receiverMap.write(new ReadWriteProtectedMultimap.CriticalSection<
                        Void,
                        EventSubscriptionTuple,
                        EventReceiverWrapper<?>,
                        SortedSetMultimap<EventSubscriptionTuple,EventReceiverWrapper<?>>>() {

                    @Override
                    public Void perform(
                            final SortedSetMultimap<EventSubscriptionTuple, EventReceiverWrapper<?>> eventReceivers) {
                        eventReceivers.remove(eventSubscriptionTuple, eventReceiverWrapper);
                        return null;
                    }

                });
            }
        };

    }

    @Override
    public Iterable<? extends EventReceiver<?>> getEventReceivers(final Path path, final String name) {
        final Iterable<EventReceiverWrapper<?>> eventReceiverWrappers;

        eventReceiverWrappers = receiverMap.read(new ReadWriteProtectedMultimap.CriticalSection<
                Iterable<EventReceiverWrapper<?>>,
                EventSubscriptionTuple,
                EventReceiverWrapper<?>,
                SortedSetMultimap<EventSubscriptionTuple, EventReceiverWrapper<?>>>() {
            @Override
            public Iterable<EventReceiverWrapper<?>> perform(
                    final SortedSetMultimap<EventSubscriptionTuple, EventReceiverWrapper<?>> eventReceivers) {
                final SortedSet<EventReceiverWrapper<?>> eventReceiverWrappers =
                        eventReceivers.get(new EventSubscriptionTuple(name, path));
                return Lists.newArrayList(eventReceiverWrappers);
            }
        });

        return eventReceiverWrappers;

    }

}
