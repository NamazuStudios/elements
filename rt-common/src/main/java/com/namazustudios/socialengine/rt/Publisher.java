package com.namazustudios.socialengine.rt;

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
     * Subscribes to a particular event.  The supplied {@link Consumer < T >} will receive zero or more events in the
     * future until the associated call to {@link Subscription#unsubscribe()}.
     *
     * @param consumer the {@link Consumer< T >} which will accept event
     * @return the {@link Subscription}
     */

    default Subscription subscribe(final Consumer<? super T> consumer) {
        return subscribe((Subscription s, T t) -> consumer.accept(t));
    }

    /**
     * Subscribes to a particular event.  The supplied {@link BiConsumer < T >} will receive zero or more events in the
     * future until the associated call to {@link Subscription#unsubscribe()}.
     *
     * @param consumer the {@link BiConsumer< T >} which will accept event
     * @return the {@link Subscription}
     */
    Subscription subscribe(BiConsumer<Subscription, ? super T> consumer);

    /**
     * Publishes the event synchronously.
     *
     * @param t the event
     */
    void publish(T t);

    /**
     * Publishes the supplied event synchronously and calls the {@link Consumer< T >} when all {@link Subscription}s have
     * been notified.
     *
     * @param t the event
     * @param onFinish the {@link Consumer< T >} to be called after all {@link Subscription}s have been notified
     */
    void publish(T t, Consumer<? super T> onFinish);

    /**
     * Clears all {@link Subscription}s and implicitly removes them from the internal pool.
     */
    void clear();

}
