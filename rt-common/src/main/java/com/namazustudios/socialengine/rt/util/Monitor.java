package com.namazustudios.socialengine.rt.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Convenience wrapper to automatically manage the state of an underlying lock.
 */
public interface Monitor extends AutoCloseable {

    /**
     * Releases the underlying {@link Lock}
     */
    @Override
    void close();

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

}
