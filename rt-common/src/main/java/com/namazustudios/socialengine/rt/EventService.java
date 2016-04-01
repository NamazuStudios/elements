package com.namazustudios.socialengine.rt;

import javax.validation.Payload;

/**
 * A service type object which is used as a place to both post and receive events.  This
 * is used by both clients and servers.
 *
 * Typically, on a server, this is used to pipe events among internal resources and
 * the actual transport may be transparent.
 *
 * Typically, on a client, an in-memory service is used by the client to handle
 * incoming events posted by the server.
 *
 * The methods on this class shall be thread safe in the sense that multiple threads may
 * both read and write to this instance while maintaining internal consistency.  Implementations
 * of this class may implement a locking strategy to this end.
 *
 * Created by patricktwohig on 3/31/16.
 */
public interface EventService {

    /**
     * Posts an event. The return value of the {@link EventHeader#getPath()} will be used
     * to determine which registered {@link EventReceiver} will receive the event.  This
     * operation is completed synchronously and will not return until all events have been
     * dispatched.
     *
     * @param event the {@link Event} to dispatch
     */
    void post(Event event);

    /**
     * Queries for all instances of {@link EventReceiver} matching the given {@link Path} and
     * event name.
     *
     * @param path the path
     * @param name the name
     *
     * @return an {@link Iterable} of {@link EventReceiver} instances
     */
    Iterable<? extends EventReceiver<?>> getEventReceivers(Path path, String name);

    /**
     * Observes the event at the given path and name.  This {@link EventService} is responsible
     * for managing the observations and owning the instances of {@link EventReceiver} provided.
     *
     * Registering an instance of {@link EventReceiver} more than once is not defined behavior
     * and it is therefore the responsiblity of the caller to avoid duplicate {@link Observation}
     *
     * @return And instance of {@link} Observation, which can be used to unregister the event.
     */
    <PayloadT>Observation observe(Path path, String name, EventReceiver<PayloadT> receiver);

}
