package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Subscription;

import java.util.function.Consumer;

/**
 * Used to implement a publish/subscribe system.  This allows multiple {@link Subscription}s to be associated with a
 * specific event.
 *
 * @param <T>
 */
public interface AsyncPublisher<T> extends Publisher<T> {

    /**
     * Publishes the supplied event asynchronously.
     *
     * @param t the event
     */
    void publishAsync(T t);

    /**
     * Publishes the supplied event asynchronously and calls the {@link Consumer<T>} when all {@link Subscription}s have
     * been notified.
     *
     * @param t the event
     * @param onFinish the {@link Consumer<T>} to be called after all {@link Subscription}s have been notified
     */
    void publishAsync(T t, Consumer<T> onFinish);

    /**
     * Publishes the supplied event asynchronously and calls the {@link Consumer<T>} when all {@link Subscription}s have
     * been notified. Additionally, this notifies for every exception thrown in the process.
     *
     * @param t the event
     * @param onFinish the {@link Consumer} to be called after all {@link Subscription}s have been notified
     * @param onException the {@link Consumer} which is notified for each exception thrown
     *
     */
    void publishAsync(T t, Consumer<T> onFinish, Consumer<Throwable> onException);

}
