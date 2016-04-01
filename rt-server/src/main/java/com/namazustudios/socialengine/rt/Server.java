package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.NotFoundException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by patricktwohig on 8/22/15.
 */
public interface Server<ResourceT extends Resource> {

    /**
     * Posts a {@link Callable} which will be executed later on the server thread pool.
     *
     * @param callable
     * @return
     */
    void post(Callable<Void> callable);

    /**
     * Observes the events at the given path.  Any events on the server's bus matching the
     * path will observe the payload.  A single {@link Observation} is generated.
     *
     * subscription will follow.
     * @param path the path
     * @param name the name of the event
     * @param eventReceiver the receiver
     * @param <PayloadT>
     *
     * @return an {@link Observation} instance
     *
     */
    <PayloadT> Observation observe(Path path, String name, EventReceiver<PayloadT> eventReceiver);

    /**
     * Gets all resources matching the given path.  The supplied path may be a wildcard path.  If none are
     * found, then this will return an empty {@link Iterable}.  That is, any {@link Iterator} instances
     * returned will immediately return false for {@link Iterator#hasNext()}.
     *
     * @param path the path
     * @return an {@link Iterable} over the internal listing of {@link Resource} objects
     */
    Iterable<ResourceT> getResources(Path path);

    /**
     * Gets a {@link Resource} at the given path.  If a single resource isn't found, then this will throw
     * and instance of {@link NotFoundException}.  The supplied path must not be a wildcard path.
     *
     * @param path the path
     * @return the {@link Resource}
     */
    ResourceT getResource(Path path);

    /**
     * Gets all resources in the server.
     *
     * @return the listing of all resources.
     */
    Iterable<ResourceT> getResources();

    /**
     * Gets the server time, typically it is the wall-clock time since the server started.  The actual
     * implementation is a detail.
     *
     * @return the server time
     */
    double getServerTime();

    /**
     * Gets the number of seconds (and fractional seconds) since the UNIX epoch, January 1, 1970.
     *
     * @return the time since the UNIX epoch
     */
    double getTimeSinceEpoch();

}
