package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.edge.EdgeServer;
import com.namazustudios.socialengine.rt.event.EventModel;
import com.namazustudios.socialengine.rt.event.ResourceAddedEvent;
import com.namazustudios.socialengine.rt.event.ResourceMovedEvent;
import com.namazustudios.socialengine.rt.event.ResourceRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The abstract implementation of the {@link Resource} class.  This includes the basic
 * functionality.
 *
 * Unless otherwise specified the various methods on this class must call their
 * appropriate superclass implementation.
 *
 * Note that becuase the {@link Server} and {@link EdgeServer} are responsible for threading
 * this class is not thread-safe.  Only one thread at a time may be calling objects implementing
 * this interface and expect the state of the application to remain consistent.
 *
 * Created by patricktwohig on 8/23/15.
 */
public abstract class AbstractResource implements Resource {

    private static final Logger logger = LoggerFactory.getLogger(AbstractResource.class);

    private final AtomicReference<Path> currentPath = new AtomicReference<>();

    private EventService eventService;

    private final ResourceId id = new ResourceId();

    @Override
    public ResourceId getId() {
        return id;
    }

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
        logger.debug("Removed resource at path " + getCurrentPath());
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
        logger.debug("Moved resource at path " + oldPath + " to path " + newPath);

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
        logger.debug("Added resource to path " + path);

    }

    /**
     * Returns this resource's current path.
     *
     * @return the current path.
     */
    @Override
    public Path getCurrentPath() {
        return currentPath.get();
    }

    /**
     * Sets the current path.
     *
     * @param currentPath the current path
     */
    public void setCurrentPath(Path currentPath) {
        this.currentPath.set(currentPath);
    }

    /**
     * Posts the given event to all of this objects' {@link EventReceiver} instances.  This ensures
     * that the event is checked and delivered to the appropriate handlers.
     *
     * The name is inferred using the {@link EventModel} annotation.
     *
     * @param <PayloadT>
     * @param event the event payloaditself
     */
    public <PayloadT> void post(final PayloadT event) {
        post(event, Event.Util.getEventNameFromObject(event));
    }

    /**
     * Posts the given event to all of this objects' {@link EventReceiver} instances.  This ensures
     * that the event is checked and delivered to the appropriate handlers.
     *
     * This method will throw an execpetion if the type is not annotated with {@link EventModel}
     *
     * @param <PayloadT>
     * @param payload the event payload itself
     * @param name the name of the event
     */
    public <PayloadT> void post(final PayloadT payload, final String name) {

        final SimpleEvent simpleEvent = SimpleEvent.builder()
                .payload(payload)
                .path(getCurrentPath())
                .name(name)
            .build();

        post(simpleEvent);

    }

    /**
     * Posts an event using the current path of the resource.
     *
     * @param event
     */
    public void post(final Event event) {

        final SimpleEvent simpleEvent = SimpleEvent.builder()
                .event(event)
                .path(getCurrentPath())
            .build();

        getEventService().post(simpleEvent);

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "currentPath='" + currentPath + '\'' +
                '}';
    }

    public EventService getEventService() {
        return eventService;
    }

    @Inject
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

}
