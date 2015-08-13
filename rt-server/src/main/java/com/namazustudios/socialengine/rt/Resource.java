package com.namazustudios.socialengine.rt;

import com.google.common.collect.ImmutableList;
import com.namazustudios.socialengine.exception.NotFoundException;

import java.util.List;
import java.util.Set;

/**
 * Created by patricktwohig on 8/12/15.
 */
public interface Resource extends AutoCloseable {

    /**
     * The path separator.  Literal value "/"
     */
    String PATH_SEPARATOR = "/";

    /**
     * Gets the methods that are supported by this Resource.  Implementations of this
     * interface may return immutable sets.  Take care when mutating the returned instances
     * such that mutability is not guaranteed.
     *
     * @return the list of methods, or an empty set if no methods are supported.
     */
    Set<String> getMethods();

    /**
     * Gets the event names sourced by this Resource.  Implementations of this
     * interface may return immutable sets.  Take care when mutating the returned instances
     * such that mutability is not guaranteed.
     *
     * @return the event names, or an empty set if not methods are supported.
     */
    Set<String> getEventNames();

    /**
     * Subscribes to {@link Event}s using the given {@link EventReceiver}.  If the
     * event is not a supported even, as returned bye {@link #getEventNames()}, then
     * this must throw an instance of {@link NotFoundException}
     *
     * @praam name the name of the event
     * @param eventReceiver the event receiver instance
     * @param <EventT>
     */
    <EventT> void subscribe(String name, EventReceiver<EventT> eventReceiver);

    /**
     * Unsubscribes from {@link Event}s using the given {@link EventReceiver}.  Note
     * that the given {@link EventReceiver} must provide a type.
     *
     * @param eventReceiver the event receiver instance
     * @param <EventT>
     */
    <EventT> void unsubscribe(EventReceiver<EventT> eventReceiver);

    /**
     * Called by the container to update the {@link Resource}.  The value passed
     * in is the time difference between the last update.
     *
     * @param deltaTime the delta time
     */
    void update(double deltaTime);

    /**
     * Closes and destroys this Resource.
     */
    void close();

    /**
     * Some utility methods used by all Resource and related instances.
     */
    final class Util {

        /**
         * Gets the path components from the given path.
         *
         * @param path the path
         * @return the components
         */
        public static List<String> componentsFromPath(final String path) {
            return ImmutableList.copyOf(path.split("/+"));
        }

    }

}
