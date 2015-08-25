package com.namazustudios.socialengine.rt;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.edge.EdgeServer;
import com.namazustudios.socialengine.rt.event.EventType;
import com.namazustudios.socialengine.rt.event.ResourceAddedEvent;
import com.namazustudios.socialengine.rt.event.ResourceMovedEvent;
import com.namazustudios.socialengine.rt.event.ResourceRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * The abstract implementation of the {@link Resource} class.  This includes the basic
 * functionality.
 *
 * Unless otherwise specified the various methods on this class must call their
 * appropriate superclass implementation.
 *
 * Note that becuase the {@link Server} and {@link EdgeServer} are responsible for threading
 * this class is not thread-safe.  Only one thread at a time may be calling objects implenting
 * this interface and expect the state of the application to remain consistent.
 *
 * Created by patricktwohig on 8/23/15.
 */
public abstract class AbstractResource implements Resource {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractResource.class);

    public static final Set<String> EVENT_NAMES = new ImmutableSet.Builder<String>()
            .add(ResourceAddedEvent.class.getSimpleName())
            .add(ResourceMovedEvent.class.getSimpleName())
            .add(ResourceRemovedEvent.class.getSimpleName())
        .build();

    private final Stopwatch stopwatch = Stopwatch.createUnstarted();

    private final SetMultimap<String, EventReceiver<?>> eventReceivers = LinkedHashMultimap.create();

    private String currentPath;

    @Override
    public void onRemove(String path) {
        final ResourceRemovedEvent resourceRemovedEvent = new ResourceRemovedEvent();
        resourceRemovedEvent.setPath(path);
        post(resourceRemovedEvent, Event.Util.getEventNameFromObject(resourceRemovedEvent));
        setCurrentPath(null);
    }

    @Override
    public void onMove(String oldPath, String newPath) {

        setCurrentPath(newPath);

        final ResourceMovedEvent resourceMovedEvent = new ResourceMovedEvent();
        resourceMovedEvent.setOldPath(oldPath);
        resourceMovedEvent.setNewPath(newPath);
        post(resourceMovedEvent, Event.Util.getEventNameFromObject(resourceMovedEvent));

    }

    @Override
    public void onAdd(String path) {

        setCurrentPath(path);

        final ResourceAddedEvent resourceAddedEvent = new ResourceAddedEvent();
        resourceAddedEvent.setPath(path);
        post(resourceAddedEvent, Event.Util.getEventNameFromObject(resourceAddedEvent));

    }

    @Override
    public Set<String> getEventNames() {
        return EVENT_NAMES;
    }

    @Override
    public <EventT> Subscription subscribe(final String name, final EventReceiver<EventT> eventReceiver) {

        if (!getEventNames().contains(name)) {
            throw new NotFoundException("Resource does not source events named " + name);
        }

        final EventReceiver<EventT> wrapper = new EventReceiverWrapper<>(eventReceiver);
        LOG.debug("Registered event receiver {}", eventReceiver);

        return new Subscription() {

            @Override
            public void release() {
                LOG.debug("Unregistered event receiver {}", eventReceiver);
                eventReceivers.remove(name, wrapper);
            }

        };

    }

    /**
     * The basic implementation of this method tracks the time between frames using
     * an instance of {@link Stopwatch} and calling {@link #doUpdate()} at the opportune
     * time.
     *
     * @see {@link Resource#onUpdate()}
     */
    @Override
    public void onUpdate() {
        stopwatch.stop();
        doUpdate();
        stopwatch.reset();
        stopwatch.start();
    }

    /**
     * Returns the elapsed time since the last update/frame.
     *
     * @return the elapsed time.
     */
    public double getElapsedTime() {
        return Math.round(getStopwatch().elapsed(TimeUnit.NANOSECONDS) * Constants.SECONDS_PER_NANOSECOND);
    }

    /**
     * Gets the {@link Stopwatch} that is used to track the time for this
     * particular resource.  Subclases may override this to provide their
     * own {@link Stopwatch} if desired.
     *
     * @return the stopwatch for this resource
     */
    public Stopwatch getStopwatch() {
        return stopwatch;
    }

    /**
     * Returns this resource's current path.
     *
     * @return the current path.
     */
    public String getCurrentPath() {
        return currentPath;
    }

    /**
     * Sets the current path.
     *
     * @param currentPath the current path
     */
    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    /**
     * Posts the given event to all of this objects' {@link EventReceiver} instances.  This ensures
     * that the event is checked and delivered to the appropriate handlers.
     *
     * The name is inferred using the {@link EventType} annotation.
     *
     * @param <EventT>
     * @param event the event itself
     */
    public <EventT> void post(final EventT event) {
        post(event, Event.Util.getEventNameFromObject(event));
    }

    /**
     * Posts the given event to all of this objects' {@link EventReceiver} instances.  This ensures
     * that the event is checked and delivered to the appropriate handlers.
     *
     * @param <EventT>
     * @param event the event itself
     * @param name the name of the event
     */
    public <EventT> void post(final EventT event, final String name) {

        if (!getEventNames().contains(name)) {
            throw new IllegalArgumentException("Event named " + name + " is not sourced by " + this);
        }

        for (EventReceiver<?> eventReceiver : eventReceivers.get(name)) {
            try {
                final Object eventObject = eventReceiver.getEventType().cast(event);
                final EventReceiver<Object> objectEventReceiver = (EventReceiver<Object>)eventReceiver;
                objectEventReceiver.receive(getCurrentPath(), name, eventObject);
            } catch (ClassCastException ex) {
                LOG.warn("Incompatible event type.", ex);
            }
        }

    }

    /**
     * Called by {@link #onUpdate()} after calculating the time since the last frame
     * using the {@link Stopwatch} instance return by this object's {@link #getStopwatch()}
     * method.
     *
     * Override this method to do useful work.
     *
     */
    protected void doUpdate() {}

    @Override
    public String toString() {
        return "AbstractResource{" +
                "type='" + getClass()  + '\'' +
                ",currentPath='" + currentPath + '\'' +
                '}';
    }

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
    private static class EventReceiverWrapper<EventT> implements EventReceiver<EventT> {

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

}
