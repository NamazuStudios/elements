package com.namazustudios.socialengine.rt.transact.unix;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Interface which wraps an {@link AtomicLong} or similar data structure.
 */
public interface UnixFSAtomicCASCounter {

    /**
     * Gets the current value, atomically.
     *
     * @return the value
     */
    long get();

    /**
     * Compares and sets the value, atomically.
     *
     * @param expect the expected value
     * @param update the new value to update
     * @return true if set, false otherwise
     */
    boolean compareAndSet(long expect, long update);

}
