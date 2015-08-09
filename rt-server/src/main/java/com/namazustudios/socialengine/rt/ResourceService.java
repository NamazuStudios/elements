package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;

import java.util.Set;

/**
 * The core server to the RT component.  Note that the server itself is
 * just responsible for dispatching requests.  It actually has no logic
 * at all for handling network code.  Other downstream projects handle
 * that process.
 *
 * Note that implementations of this interface should be considered thread
 * safe.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface ResourceService {

    /**
     * Gets the request handler for the given path.  The path is
     * user defined and is ultimately the destination for a {@link RequestHeader}
     *
     * @param requestHeader the request header.
     * @return the RequestPathHandler to handle the path
     *
     * @throws {@link NotFoundException} if the given handler cannot be found
     * @throws {@link InvalidDataException} if the handler is found, but does not match the payload type
     */
    RequestPathHandler<?> getPathHandler(RequestHeader requestHeader);

    /**
     * Gets a set of {@link EventReceiver} instances for the given {@link EventHeader}.
     *
     * @param eventHeader the event header
     *
     * @return
     */
    Set<EventReceiver<?>> getEventReceiver(EventHeader eventHeader);

    /**
     * Subcribes the given event receiver to events at the given path.
     *
     * @param path the path of the {@link Resource}
     * @param name the name of the event
     * @param eventReceiver
     * @param <EventT>
     */
    <EventT> void subscribe(String path, String name, EventReceiver<EventT> eventReceiver);

    /**
     *
     * @param eventReceiver
     * @param <EventT>
     */
    <EventT> void unsubscribe(EventReceiver<EventT> eventReceiver);

    /**
     * Adds a {@link Resource} to this resource service.
     *
     * @param resource the resource
     */
    void addResource(Resource resource);

    /**
     * Moves the given resource to the given new destination.
     *
     * This throws an instance of {@link NotFoundException} if the resource path
     * is not found.
     *
     * @param path the resource path
     * @param newDestination the new destination path of the resource.
     *
     */
    void moveResource(String path, String newDestination);

    /**
     * Removes a {@link Resource} instance from this resource service.
     *
     * @param resource
     */
    void removeResource(Response resource);

}
