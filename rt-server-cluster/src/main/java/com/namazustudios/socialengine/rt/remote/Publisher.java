package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.Subscription;

import java.util.function.Consumer;

/**
 * Used to implement a publish/subscribe system.  This allows multiple {@link Subscription}s to be associated with a
 * specific event.
 *
 * @param <T>
 */
public interface Publisher<T> {

    /**
     * Subscribes to a particular event.  The supplied {@link Consumer<T>} will receive zero or more events in the
     * future until the associated call to {@link Subscription#unsubscribe()}.
     *
     * @param consumer the {@link Consumer<T>} which will accept event
     * @return the {@link Subscription}
     */
    Subscription subscribe(Consumer<T> consumer);

    /**
     * Publishes the event synchronously.
     *
     * @param t the event
     */
    void publish(T t);

    /**
     * Publishes the supplied event asynchronously.
     *
     * @param t the event
     */
    void publishAsync(T t);

    /**
     * Publishes the supplied event synchronously and calls the {@link Consumer<T>} when all {@link Subscription}s have
     * been notified.
     *
     * @param t the event
     * @param onFinish the {@link Consumer<T>} to be called after all {@link Subscription}s have been notified
     */
    void publish(T t, Consumer<T> onFinish);

    /**
     * Publishes the supplied event asynchronously and calls the {@link Consumer<T>} when all {@link Subscription}s have
     * been notified.
     *
     * @param t the event
     * @param onFinish the {@link Consumer<T>} to be called after all {@link Subscription}s have been notified
     */
    void publishAsync(T t, Consumer<T> onFinish);

}
