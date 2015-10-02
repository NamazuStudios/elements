package com.namazustudios.socialengine.rt;

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
     * path will receive the payload.  A single {@link Observation} is generated.
     *
     * subscription will follow.
     * @param path the path
     * @param name the name of the event
     * @param eventReceiver the receiver
     * @param <PayloadT>
     *
     * @return mapping of {@link Path} to {@link Subscription instances , which can be used to unsubscribe from the event pool
     *
     */
    <PayloadT> Observation observe(Path path, String name, EventReceiver<PayloadT> eventReceiver);

    /**
     * Subscribes the event receiver to the given paths, recursively if necessary.
     *
     * If the given path is a wildcard path, this will recursively subscribe to
     * all resources matching that particular path.  This means that a single subscription
     * will actually subscribe to the actual resource.  If the resource is moved, then the
     * subscription will follow.
     *
     * @param path the path
     * @param name the name of the event
     * @param eventReceiver the receiver
     * @param <PayloadT>
     *
     * @return mapping of {@link Path} to {@link Subscription instances , which can be used to unsubscribe from the event pool
     *
     */
    <PayloadT> List<Subscription> subscribe(Path path, String name, EventReceiver<PayloadT> eventReceiver);

    Iterable<ResourceT> getResources(Path path);

    ResourceT getResource(Path path);

    Iterable<ResourceT> getResources();
}
