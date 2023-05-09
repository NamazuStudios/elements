package dev.getelements.elements.rt.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * A thread-safe implementation of {@link LazyValue}.
 *
 * This uses a {@link Lock} to perform the synchronization.
 *
 * @param <T> the type of value to supply
 */
public class ThreadSafeLazyValue<T> implements Supplier<T> {

    private volatile Supplier<T> tWrappedSupplier;

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

        tWrappedSupplier = new Supplier<T>() {

            @Override
            public T get() {
                try (var monitor = Monitor.enter(lock)) {

                    final T t;

                    if (tWrappedSupplier == this) {
                        t = tSupplier.get();
                        tWrappedSupplier = () -> t;
                    } else {
                        t = tWrappedSupplier.get();
                    }

                    return t;

                }
            }

            @Override
            public String toString() {
                return "UNASSIGNED";
            }

        };
    }

    @Override
    public T get() {
        return tWrappedSupplier.get();
    }

}
