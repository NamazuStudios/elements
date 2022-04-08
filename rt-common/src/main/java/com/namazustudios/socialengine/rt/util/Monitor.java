package com.namazustudios.socialengine.rt.util;

import com.namazustudios.socialengine.rt.exception.UncheckedInterruptedException;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

/**
 * Convenience wrapper to automatically manage the state of an underlying lock, series of locks, or semaphores. This
 * can be used to ensure that multiple locks or semaphores are acquired in consistent order (avoiding deadlock) and
 * are automatically released when the critical section of code executes.
 */
public interface Monitor extends AutoCloseable {

    /**
     * Releases the underlying {@link Lock} or {@link Semaphore}
     */
    @Override
    void close();

    /**
     * Acquires a monitor which will release the lock after this monitor closes.
     *
     * {@link #enter(Lock)}
     *
     * @param lock the lock
     * @return a new Monitor
     */
    default Monitor then(final Lock lock) {
        return then(enter(lock));
    }

    /**
     * Acquires a monitor which will release the semaphore after this monitor closes.
     *
     * {@link #enter(Semaphore)}
     *
     * @param semaphore the semaphore
     * @return a new Monitor
     */
    default Monitor then(final Semaphore semaphore) {
        return then(enter(semaphore));
    }

    /**
     * Acquires a monitor which will release the lock after this monitor closes.
     *
     * {@link #enter(Semaphore,int)}
     *
     * @param semaphore the semaphore
     * @param permits the permits to acquire
     * @return a new Monitor
     */
    default Monitor then(final Semaphore semaphore, final int permits) {
        return then(enter(semaphore, permits));
    }

    /**
     * Acquires a monitor which will release the lock after this monitor closes. This ensures that when multiple
     * monitors are chained together then they are released in the reverse order.
     *
     * {@link #enter(Semaphore,int)}
     *
     * @param other the other monitor
     * @return a new Monitor
     */
    default Monitor then(final Monitor other) {
        return () -> {
            other.close();
            close();
        };
    }

    /**
     * Generates a {@link Monitor} from the supplied {@link Lock} instance.
     *
     * @param lock the {@link Lock} instance
     * @return a {@link Monitor} which will immediately unlock
     */
    static Monitor enter(final Lock lock) {
        lock.lock();
        return lock::unlock;
    }

    /**
     * Generates a {@link Monitor} from the supplied {@link Semaphore} instance.
     *
     * @param semaphore the {@link Semaphore} instance
     * @return a {@link Monitor} which will immediately unlock
     */
    static Monitor enter(final Semaphore semaphore) {
        try {
            semaphore.acquire();
            return semaphore::release;
        } catch (InterruptedException ex) {
            throw new UncheckedInterruptedException(ex);
        }
    }
    /**
     * Generates a {@link Monitor} from the supplied {@link Semaphore} instance.
     *
     * @param semaphore the {@link Semaphore} instance
     * @param permits the number of permits to acquire and subsequently release
     * @return a {@link Monitor} which will immediately unlock
     */
    static Monitor enter(final Semaphore semaphore, final int permits) {
        try {
            semaphore.acquire(permits);
            return () -> semaphore.release(permits);
        } catch (InterruptedException ex) {
            throw new UncheckedInterruptedException(ex);
        }
    }

}
