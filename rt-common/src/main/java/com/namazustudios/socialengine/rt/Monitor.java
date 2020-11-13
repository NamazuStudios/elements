package com.namazustudios.socialengine.rt;

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

}
