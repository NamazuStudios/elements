package com.namazustudios.socialengine.rt;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.namazustudios.socialengine.rt.edge.EdgeServer;

import java.util.List;
import java.util.regex.Pattern;

/**
 * A Resource is essentially a type that is capable primarily of both
 * receiving {@link Request} instances to produce {@link Response}
 * instances.
 *
 * Additionally, a Resource can be the source of {@link Event} objects
 * which can be transmitted from the server to the client, or can be transmitted
 * to other {@link Resource} instances via the {@link EventReceiver} inteface.
 *
 * Typically instances of Resource have their own scope, and communicate with other Resources onlyt
 * through either events or requests.  This allows the {@link Server} or {@link EdgeServer} to parallelize
 * and distribute the resources across threads, or even physical machines.
 *
 * Once a resource is no longer needed, it is necessary to destroy the
 * resource using the {@link AutoCloseable#close()} method.
 *
 * Created by patricktwohig on 8/8/15.
 */
public interface Resource extends AutoCloseable {

    /**
     * Subscribes to {@link Event}s using the given {@link EventReceiver}.
     *
     * This method should not be used directly, but rather should be managed by
     * the server instance.
     *
     * @praam name the name of the event
     * @param eventReceiver the event receiver instance
     * @param <EventT>
     *
     */
    <EventT> Subscription subscribe(String name, EventReceiver<EventT> eventReceiver);

    /**
     * Called when he resource has been added to the {@link ResourceService}.
     *
     * @param path the path
     */
    void onAdd(String path);

    /**
     * Called by the container to upate the {@link Resource}.  The resource is responsible
     * for keeping track of its own time internally and updating the resource accordingly.
     */
    void onUpdate();

    /**
     * Called when the resource has been moved to a new path.  In the event
     * of an exception the {@link ResourceService} guarantees that the state
     * of the program remains consistent.
     *
     * @param oldPath the old path
     * @param newPath the new path
     *
     */
    void onMove(String oldPath, String newPath);

    /**
     * Called when the resource has been removed by the {@link ResourceService}.
     *
     * @param path the path
     */
    void onRemove(String path);

    /**
     * Closes and destroys this Resource.  A resource, once destroyed, cannot
     * be used again.
     */
    void close();

}
