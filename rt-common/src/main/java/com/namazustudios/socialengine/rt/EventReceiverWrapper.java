package com.namazustudios.socialengine.rt;

import java.util.UUID;

/**
 * Wraps an instance of {@link EventReceiver} such that it always compares
 * equality using operator == and {@link System#identityHashCode(Object)}.
 *
 * This ensures that the wrap {@link EventReceiver} can only subscribe once
 * to the same event name, and secondly that the {@link EventReceiver} can
 * be reliably removed from the pool.
 *
 * @param <EventT>
 */
public class EventReceiverWrapper<EventT> implements EventReceiver<EventT> {

    final EventReceiver<EventT> wrapped;

    final UUID uuid = UUID.randomUUID();

    public EventReceiverWrapper(EventReceiver<EventT> wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Class<EventT> getEventType() {
        return wrapped.getEventType();
    }

    @Override
    public void receive(String path, String name, EventT event) {
        wrapped.receive(path, name, event);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(wrapped);
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof EventReceiverWrapper)) {
            return false;
        }

        final EventReceiverWrapper<?> other = (EventReceiverWrapper<?>)obj;
        return wrapped == other.wrapped;

    }

    @Override
    public String toString() {
        return "EventReceiverWrapper{" +
                "wrapped=" + wrapped +
                ", uuid=" + uuid +
                '}';
    }

}
