package dev.getelements.elements.rt.transact.unix;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Interface which behaves similar to an {@link AtomicLong}, but with reduced functionality. This may be a pure memory
 * based implementation, as backed by {@link AtomicLong}, or it may be backed by one obtained from
 * {@link UnixFSMemoryUtils#getAtomicLong(ByteBuffer)}
 */
public interface UnixFSAtomicLong {

    /**
     * Gets the current value, atomically.
     *
     * @return the value
     */
    long get();

    /**
     * Sets the value of, atomically.
     *
     * @param value the value to set
     */
    default void set(final long value) {
        long current;
        do current = get(); while (!compareAndSet(current, value));
    }

    /**
     * Compares and sets the value, atomically.
     *
     * @param expect the expected value
     * @param update the new value to update
     * @return true if set, false otherwise
     */
    boolean compareAndSet(long expect, long update);

    /**
     * Creates a basic {@link UnixFSAtomicLong}.
     *
     * @return a {@link UnixFSAtomicLong} instance
     */
    static UnixFSAtomicLong basic() {
        return wrap(new AtomicLong());
    }

    /**
     * Wraps an instance of {@link AtomicLong} into an instance of {@link UnixFSAtomicLong}.
     *
     * @return a {@link UnixFSAtomicLong} instance
     */
    static UnixFSAtomicLong wrap(final AtomicLong atomicLong) {
        return new UnixFSAtomicLong() {

            @Override
            public long get() {
                return atomicLong.get();
            }

            @Override
            public void set(final long value) {
                atomicLong.set(value);
            }

            @Override
            public boolean compareAndSet(long expect, long update) {
                return atomicLong.compareAndSet(expect, update);
            }

            @Override
            public String toString() {
                return Long.toString(atomicLong.get());
            }

        };
    }

}
