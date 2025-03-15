package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Subscription;
import org.slf4j.Logger;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A thread-safe {@link Publisher} which uses a standard {@link ConcurrentLinkedDeque} to perform operations.
 *
 * When iterating the list of subscribers (such as when dispatching events) no locks are required as iteration
 * of the list can happen without any locks. The iterator is weakly consistent as per the implementation of
 * of {@link ConcurrentLinkedDeque} and unsubscription happens in O(n) (worst case).
 *
 * @param <T>
 */
public class ConcurrentDequePublisher<T> extends AbstractPublisher<T> implements Iterable<Subscription> {

    private final Deque<DequeSubscription<T>> deque = new ConcurrentLinkedDeque<>();

    public ConcurrentDequePublisher() {
        this(ConcurrentDequePublisher.class);
    }

    public ConcurrentDequePublisher(Class<?> cls) {
        super(cls);
    }

    public ConcurrentDequePublisher(Logger logger) {
        super(logger);
    }

    @Override
    public Subscription subscribe(final BiConsumer<Subscription, ? super T> consumer) {
        final var subscription = new DequeSubscription<>(this, consumer);
        deque.add(subscription);
        return subscription;
    }

    @Override
    public void clear() {
        deque.clear();
    }

    @Override
    public void publish(
            final T t,
            final Consumer<? super T> onFinish,
            final Consumer<Throwable> onException) {

        final var iterator = deque.iterator();

        while (iterator.hasNext()) {
            final DequeSubscription<T> dequeSubscription = iterator.next();
            dequeSubscription.accept(iterator, t, onException);
        }

        try {
            onFinish.accept(t);
        } catch (Exception ex) {
            handleException(onException, ex);
        }

    }

    @Override
    public Iterator<Subscription> iterator() {
        return new Iterator<>() {

            final Iterator<DequeSubscription<T>> iterator = deque.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Subscription next() {
                return iterator.next();
            }

        };
    }

    private record DequeSubscription<T>(
            ConcurrentDequePublisher<T> publisher,
            BiConsumer<Subscription, ? super T> consumer) implements Subscription {

        public void accept(
                final Iterator<DequeSubscription<T>> iterator,
                final T value,
                final Consumer<Throwable> onException) {
            try {
                consumer.accept(iterator::remove, value);
            } catch (Exception ex) {
                publisher.handleException(onException, ex);
            }
        }

        @Override
        public void unsubscribe() {
            if (!publisher.deque.remove(this)) {
                publisher.logger.warn("Already unsubscribed.");
            }
        }

    }

}
