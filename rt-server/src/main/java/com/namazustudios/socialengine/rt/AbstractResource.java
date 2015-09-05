package com.namazustudios.socialengine.rt;

import com.google.common.base.Stopwatch;
import com.namazustudios.socialengine.rt.edge.EdgeServer;
import com.namazustudios.socialengine.rt.event.EventModel;
import com.namazustudios.socialengine.rt.event.ResourceAddedEvent;
import com.namazustudios.socialengine.rt.event.ResourceMovedEvent;
import com.namazustudios.socialengine.rt.event.ResourceRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Stopwatch stopwatch = Stopwatch.createUnstarted();

    private final ResourceEventSubscriberMap defaultResourceEventReceiverMap = new DefaultResourceEventReceiverMap();

    private Path currentPath;

    /**
     * This posts an instance of {@link ResourceRemovedEvent} and sets the current path.
     *
     * @see {@link Resource#onRemove(Path)}
     *
     * @param path the path
     */
    @Override
    public void onRemove(Path path) {
        final ResourceRemovedEvent resourceRemovedEvent = new ResourceRemovedEvent();
        resourceRemovedEvent.setPath(path.toString());
        post(resourceRemovedEvent, Event.Util.getEventNameFromObject(resourceRemovedEvent));
        setCurrentPath(null);
        LOG.debug("Removed resource at path " + getCurrentPath());
    }

    /**
     * This posts and instance of {@link ResourceMovedEvent} and sets the current path.
     *
     * @see {@link Resource#onMove(Path, Path)}
     *
     * @param oldPath the old path
     * @param newPath the new path
     */
    @Override
    public void onMove(final Path oldPath, final Path newPath) {

        setCurrentPath(newPath);

        final ResourceMovedEvent resourceMovedEvent = new ResourceMovedEvent();
        resourceMovedEvent.setOldPath(oldPath.toString());
        resourceMovedEvent.setNewPath(newPath.toString());
        post(resourceMovedEvent, Event.Util.getEventNameFromObject(resourceMovedEvent));
        LOG.debug("Moved resource at path " + oldPath + " to path " + newPath);

    }

    /**
     * This posts and instance of {@link ResourceAddedEvent} and sets the current path.
     *
     * @see {@link Resource#onAdd(Path)}
     *
     * @param path the path
     */
    @Override
    public void onAdd(Path path) {

        setCurrentPath(path);

        final ResourceAddedEvent resourceAddedEvent = new ResourceAddedEvent();
        resourceAddedEvent.setPath(path.toString());
        post(resourceAddedEvent, Event.Util.getEventNameFromObject(resourceAddedEvent));
        LOG.debug("Added resource to path " + path);

    }

    /**
     *
     * @see {@link Resource#subscribe(String, EventReceiver)}.
     *
     * @param desiredName the name
     * @param eventReceiver the event receiver instance
     *
     * @param <EventT>
     *
     * @return the {@link Subscription} object
     */
    @Override
    public <EventT> Subscription subscribe(final String desiredName, final EventReceiver<EventT> eventReceiver) {
        return defaultResourceEventReceiverMap.subscribe(desiredName, eventReceiver);
    }

    /**
     * The basic implementation of this method tracks the time between frames using
     * an instance of {@link Stopwatch} and calling {@link #doUpdate(double deltaTme)}
     * at the opportune time.
     *
     * @see {@link Resource#onUpdate()}
     */
    @Override
    public void onUpdate() {
        stopwatch.stop();
        final double deltaTime = getElapsedTime();
        doUpdate(deltaTime);
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
    @Override
    public Path getCurrentPath() {
        return currentPath;
    }

    /**
     * Sets the current path.
     *
     * @param currentPath the current path
     */
    public void setCurrentPath(Path currentPath) {
        this.currentPath = currentPath;
    }

    /**
     * Posts the given event to all of this objects' {@link EventReceiver} instances.  This ensures
     * that the event is checked and delivered to the appropriate handlers.
     *
     * The name is inferred using the {@link EventModel} annotation.
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
     * This method will throw an execpetion if the type is not annotated with {@link EventModel}
     *
     * @param <EventT>
     * @param event the event itself
     * @param name the name of the event
     */
    public <EventT> void post(final EventT event, final String name) {
        defaultResourceEventReceiverMap.post(getCurrentPath(), event, name);
    }

    /**
     * Called by {@link #onUpdate()} after calculating the time since the last frame
     * using the {@link Stopwatch} instance return by this object's {@link #getStopwatch()}
     * method.
     *
     * Override this method to do useful work.
     *
     * @param deltaTime the time since the last update
     *
     */
    protected void doUpdate(final double deltaTime) {}

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "currentPath='" + currentPath + '\'' +
                '}';
    }

}
