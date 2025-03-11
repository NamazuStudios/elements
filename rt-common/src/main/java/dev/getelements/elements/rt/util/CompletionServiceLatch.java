package dev.getelements.elements.rt.util;

import dev.getelements.elements.rt.exception.InternalException;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * This is a latch-type object which will allow the caller to wrap {@link Runnable} and {@link Callable<?>} instances
 * which can be later monitored for the completion of all tasks.
 */
public class CompletionServiceLatch {

    private final Semaphore semaphore;

    private final AtomicBoolean accepting = new AtomicBoolean(true);

    private final ConcurrentMap<Object, Future<?>> pendingFutures = new ConcurrentHashMap<>();

    public CompletionServiceLatch() {
        this(new Semaphore(Integer.MAX_VALUE, true));
    }

    public CompletionServiceLatch(final Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    private void acquire() {
        try {

            semaphore.acquire(2);

            if (!accepting.get()) {
                semaphore.release();
                throw new IllegalStateException("Not accepting new work.");
            }

        } catch (InterruptedException ex) {
            throw new InternalException(ex);
        } finally {
            semaphore.release();
        }
    }

    /**
     * Wraps a {@link Callable<T>} and returns it. If {@link #stop()} has been called, then this throws an instance of
     * {@link IllegalStateException}.
     *
     * @param callable the {@link Callable<T>} to wrap
     * @param <T>
     * @return a wrapped {@link Callable<T>} instance
     */
    public <FutureT extends Future<T>,T> FutureT enqueue(final Callable<T> callable,
                                                         final Function<Callable<T>, FutureT> futureSupplier) {

        final Object key = new Object();

        final Callable<T> wrapped = () -> {
            try {
                return callable.call();
            } finally {
                pendingFutures.remove(key);
                semaphore.release();
            }
        };

        final FutureT future;

        acquire();

        try {
            future = futureSupplier.apply(wrapped);
        } catch (Exception ex) {
            semaphore.release();
            throw ex;
        }

        pendingFutures.put(key, future);
        return future;

    }

    /**
     * Wraps a {@link Runnable} and returns it. If {@link #stop()} has been called, then this throws an instance of
     * {@link IllegalStateException}.
     *
     * @param runnable the {@link Runnable} to wrap
     * @return a wrapped {@link Runnable} instance
     */

    public <FutureT extends Future<T>,T> FutureT enqueue(final Runnable runnable,
                                                         final Function<Runnable, FutureT> futureSupplier) {

        acquire();

        final var key = new Object();

        final Runnable wrapped = () -> {
            try {
                runnable.run();
            } finally {
                semaphore.release();
            }
        };

        final FutureT future;

        acquire();

        try {
            future = futureSupplier.apply(wrapped);
        } catch (Exception ex) {
            semaphore.release();
            throw ex;
        }

        pendingFutures.put(key, future);
        return future;

    }

    /**
     * Stops the latch and prevents it from accepting any more jobs. Before stopping, this will wait for all current
     * tasks to finish
     */
    public void stop() {
        try {
            semaphore.acquire(Integer.MAX_VALUE);
            accepting.set(false);
            pendingFutures.values().forEach(f -> f.cancel(true));
            pendingFutures.clear();
        } catch (InterruptedException e) {
            throw new InternalException(e);
        } finally {
            semaphore.release(Integer.MAX_VALUE);
        }
    }

}
