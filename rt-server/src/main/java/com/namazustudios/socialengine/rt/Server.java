package com.namazustudios.socialengine.rt;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Created by patricktwohig on 8/22/15.
 */
public interface Server {

    /**
     * Posts a {@link Callable} which will be executed at some point later
     * in the server's thread pool.
     *
     * @param callable the callable to run
     * @return a {@link Future<T>} representing the return.
     */
    <T> Future<T> post(Callable<T> callable);

    /**
     * Posts a {@link Runnable} and returns no result.  This exists to make
     * the usage of lambda expressions simpler (does not require a return value).
     *
     * @param runnable the runnable to run
     */
    default Future<Void> postV(final Runnable runnable) {
        return post(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Observes the events at the given path.  Any events on the server's bus matching the
     * path will observe the payload.  A single {@link Observation} is generated.
     *
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
     * Observes the events at the given path.  Any events on the server's bus matching the path
     * will observe the payload.  A single {@link Observation} is generated.  This is a convienicne
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
