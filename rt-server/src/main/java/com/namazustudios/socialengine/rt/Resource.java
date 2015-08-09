package com.namazustudios.socialengine.rt;

import java.util.Set;

/**
 * A Resource is essentially a type that is capable primarly of both
 * receiving {@link Request} instances to produce {@link Response}
 * instances.
 *
 * Additionally, a Resource can be the source of {@link EventHeader} objects
 * which can be transmitted from the server to the client.
 *
 * Typically instances of Resource have their own scope, and
 * communicate primarily with other Resources through either events
 * or requests.
 *
 * Once a resource is no longer needed, it is necessary to destroy the
 * resource using the {@link AutoCloseable#close()} method.
 *
 * Created by patricktwohig on 8/8/15.
 */
public interface Resource extends AutoCloseable {

    /**
     * Gets the methods that are supported by this Resource.
     *
     * @return
     */
    Set<String> getMethods();

    /**
     * Gets the event names sourced by this resource.
     *
     * @return the event names
     */
    Set<String> getEventNames();

    /**
     * Gets the RequestHandler for the method.
     *
     * @param method
     * @return
     */
    RequestPathHandler<?> getHandler(final String method);

    /**
     * Subscribes to {@link Event}s using the given {@link EventReceiver}.
     *
     * @praam name the name of the event
     * @param eventReceiver the event receiver instance
     * @param <EventT>
     */
    <EventT> void subscribe(String name, EventReceiver<EventT> eventReceiver);

    /**
     * Unsubscribes from {@link Event}s using the given {@link EventReceiver}.
     *
     * @param name the name of the event
     * @param eventReceiver the event receiver instance
     * @param <EventT>
     */
    <EventT> void unsubscribe(String name, EventReceiver<EventT> eventReceiver);

    /**
     * Called by the container to update the {@link Resource}.  The value passed
     * in is the time difference between the last update.
     *
     * @param deltaTime the delta time
     */
    void update(double deltaTime);

}
