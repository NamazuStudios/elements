package com.namazustudios.socialengine.rt.transact.unix;

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

}
