package com.namazustudios.socialengine.rt;

import com.google.common.collect.Lists;
import com.google.common.collect.SortedSetMultimap;

import java.util.SortedSet;

/**
 * Created by patricktwohig on 9/5/15.
 */
public class DefaultClientEventReceiverMap implements ClientEventReceiverMap {

    private ReadWriteProtectedSortedSetMultimap<EventSubscriptionTuple, EventReceiverWrapper<?>> receiverMap =
            new ReadWriteProtectedSortedSetMultimap<>();

    @Override
    public <EventT> Subscription subscribe(Path path, String name, final EventReceiver<EventT> eventReceiver) {

        final EventSubscriptionTuple eventSubscriptionTuple = new EventSubscriptionTuple(name, path);
        final EventReceiverWrapper<EventT> eventReceiverWrapper = new EventReceiverWrapper<>(eventReceiver);

        receiverMap.write(new ReadWriteProtectedObject.CriticalSection<Void, SortedSetMultimap<EventSubscriptionTuple,EventReceiverWrapper<?>>>() {
            @Override
            public Void perform(SortedSetMultimap<EventSubscriptionTuple, EventReceiverWrapper<?>> protectedObject) {
                protectedObject.put(eventSubscriptionTuple, eventReceiverWrapper);
                return null;
            }
        });

        return new Subscription() {
            @Override
            public void release() {
                receiverMap.write(new ReadWriteProtectedObject.CriticalSection<Void, SortedSetMultimap<EventSubscriptionTuple,EventReceiverWrapper<?>>>() {
                    @Override
                    public Void perform(final SortedSetMultimap<EventSubscriptionTuple, EventReceiverWrapper<?>> eventReceivers) {
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

        eventReceiverWrappers = receiverMap.read(new ReadWriteProtectedObject.CriticalSection<Iterable<EventReceiverWrapper<?>>, SortedSetMultimap<EventSubscriptionTuple, EventReceiverWrapper<?>>>() {
            @Override
            public Iterable<EventReceiverWrapper<?>> perform(SortedSetMultimap<EventSubscriptionTuple, EventReceiverWrapper<?>> protectedObject) {
                final SortedSet<EventReceiverWrapper<?>> eventReceiverWrappers = protectedObject.get(new EventSubscriptionTuple(name, path));
                return Lists.newArrayList(eventReceiverWrappers);
            }
        });

        return eventReceiverWrappers;

    }

}
