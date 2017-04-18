package com.namazustudios.socialengine.rt;

import javax.validation.Payload;
import java.util.function.Consumer;

/**
 * A service type object which is used as a place to both post and receive events.  This
 * is used by both clients and servers.
 *
 * Typically, on a server, this is used to pipe events among worker resources and
 * the actual transport may be transparent.
 *
 * Typically, on a client, an in-memory service is used by the client to handle
 * incoming events posted by the server.
 *
 * The methods on this class shall be thread safe in the sense that multiple threads may
 * both read and write to this instance while maintaining worker consistency.  Implementations
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

    /**
     * Observes the events at the given path.  Any events on the server's bus matching the path
     * will observe the payload.  A single {@link Observation} is generated.  This is a convenience
     * method assist in using {@link #observe(Path, String, EventReceiver)} when used with lambda
     * expressions
     *
     * @param path the path
     * @param name the name of the event
     * @param payloadTClass the payload type
     * @param payloadTConsumer the consumer
     * @param <PayloadT> the payload itself
     * @return an {@link Observation} instance
     */
    default <PayloadT> Observation observe(
            final Path path, final String name,
            final Class<PayloadT> payloadTClass,
            final Consumer<PayloadT> payloadTConsumer) {
        return observe(path, name, new EventReceiver<PayloadT>() {

            @Override
            public Class<PayloadT> getEventType() {
                return payloadTClass;
            }

            @Override
            public void receive(Event event) {
                final PayloadT payload = payloadTClass.cast(event.getPayload());
                payloadTConsumer.accept(payload);
            }

        });
    }

}
