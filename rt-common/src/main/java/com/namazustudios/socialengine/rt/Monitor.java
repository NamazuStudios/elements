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

    /**
     * Gets a {@link Condition} with an arbitrary name associated with the supplied {@link Monitor}.
     *
     * @param name the name of the {@link Condition}
     * @return the {@link Condition}
     */
    Condition getCondition(final String name);

}
