package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Subscription;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Publishes events to multiple subscribers.  This interface defines a simple synchronous style publisher/subscriber
 * system.
 *
 * @param <T>
 */
public interface Publisher<T> {

    /**
     * Subscribes to a particular event.  The supplied {@link Consumer } will receive zero or more events in the
     * future until the associated call to {@link Subscription#unsubscribe()}.
     *
     * @param consumer the {@link Consumer} which will accept event
     * @return the {@link Subscription}
     */

    default Subscription subscribe(final Consumer<? super T> consumer) {
        return subscribe((Subscription s, T t) -> consumer.accept(t));
    }

    /**
     * Subscribes to a particular event.  The supplied {@link BiConsumer } will receive zero or more events in the
     * future until the associated call to {@link Subscription#unsubscribe()}.
     *
     * @param consumer the {@link BiConsumer} which will accept event
     * @return the {@link Subscription}
     */
    Subscription subscribe(BiConsumer<Subscription, ? super T> consumer);

    /**
     * Clears all {@link Subscription}s and implicitly removes them from the internal pool. Implementing this interface
     * method is optional, and therefore this may throw an instance of {@link UnsupportedOperationException}.
     */
    void clear();

    /**
     * Publishes the event synchronously.
     *
     * @param t the event
     */
    void publish(T t);

    /**
     * Publishes the supplied event synchronously and calls the {@link Consumer} when all {@link Subscription}s
     * have been notified.
     *
     * @param t the event
     * @param onFinish the {@link Consumer} to be called after all {@link Subscription}s have been notified
     */
    void publish(T t, Consumer<? super T> onFinish);

    /**
     * Publishes the supplied event synchronously and calls the {@link Consumer} when all {@link Subscription}s have
     * been notified. Additionally, this notifies for every exception thrown in the process.
     *
     * @param t the event
     * @param onFinish the {@link Consumer} to be called after all {@link Subscription}s have been notified
     * @param onException the {@link Consumer} which is notified for each exception thrown
     */
    void publish(T t, Consumer<? super T> onFinish, Consumer<Throwable> onException);

}
