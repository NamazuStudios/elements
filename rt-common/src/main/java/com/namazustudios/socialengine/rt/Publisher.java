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

    default <U extends T> Subscription subscribe(final Consumer<? super U> consumer) {
        return subscribe((Subscription s, U u) -> consumer.accept(u));
    }

    /**
     * Subscribes to a particular event.  The supplied {@link BiConsumer < T >} will receive zero or more events in the
     * future until the associated call to {@link Subscription#unsubscribe()}.
     *
     * @param consumer the {@link BiConsumer< T >} which will accept event
     * @return the {@link Subscription}
     */
    <U extends T> Subscription subscribe(BiConsumer<Subscription, ? super U> consumer);

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
    void publish(T t, Consumer<T> onFinish);

    /**
     * Clears all {@link Subscription}s and implicitly removes them from the internal pool.
     */
    void clear();

    /**
     * Singleton dummy {@link Publisher}
     */
    Publisher<Object> DUMMY = new Publisher<Object>() {

        private final Subscription subscription = () -> {};

        @Override
        public <U> Subscription subscribe(BiConsumer<Subscription, ? super U> consumer) {
            return subscription;
        }

        @Override
        public void publish(Object dummy) {}

        @Override
        public void publish(Object dummy, Consumer<Object> onFinish) {}

        @Override
        public void clear() {}

    };

    /**
     * Returns a dummy {@link Publisher}
     *
     * @param <DummyT> a new dummy publisher
     * @return
     */
    static <DummyT> Publisher<DummyT> dummy() {
        return (Publisher<DummyT>) DUMMY;
    }

}
