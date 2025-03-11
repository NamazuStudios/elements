package dev.getelements.elements.sdk.util;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * A thread-safe implementation of {@link SimpleLazyValue}.
 *
 * This uses a {@link Lock} to perform the synchronization.
 *
 * @param <T> the type of value to supply
 */
public class ThreadSafeLazyValue<T> implements LazyValue<T> {

    private Lock lock;

    private Supplier<T> tSupplier;

    volatile T tObject = (T) SimpleLazyValue.UNASSIGNED;

    /**
     * Creates an instance of {@link ThreadSafeLazyValue<T>} with default lock.
     *
     * This constructor uses the constructor {@link ReentrantLock#ReentrantLock()} to construct the lock using the
     * default fairness option.
     *
     * @param tSupplier the {@link Supplier<T>} which supplies the value.
     */
    public ThreadSafeLazyValue(final Supplier<T> tSupplier) {
        this(tSupplier, new ReentrantLock());
    }

    /**
     * Creates an instance of {@link ThreadSafeLazyValue<T>} with default lock.
     *
     * This constructor allows for the specification of a custom {@link Lock} instance.
     *
     * @param tSupplier the {@link Supplier<T>} which supplies the value.
     */
    public ThreadSafeLazyValue(final Supplier<T> tSupplier, final Lock lock) {
        this.lock = lock;
        this.tSupplier = tSupplier;
    }

    @Override
    public T get() {
        if (tObject == SimpleLazyValue.UNASSIGNED) {
            try (final var monitor = Monitor.enter(lock)) {
                if (tObject == SimpleLazyValue.UNASSIGNED) {
                    var result = tObject = tSupplier.get();
                    lock = null;
                    tSupplier = null;
                    return result;
                } else{
                    return tObject;
                }
            }
        } else {
            return tObject;
        }
    }

    @Override
    public Optional<T> getOptional() {
        final T result = tObject;
        return result == SimpleLazyValue.UNASSIGNED ? Optional.empty() : Optional.of(result);
    }

}
