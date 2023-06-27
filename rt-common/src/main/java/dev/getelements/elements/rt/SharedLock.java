package dev.getelements.elements.rt;

import dev.getelements.elements.rt.util.Monitor;

import java.util.concurrent.locks.Lock;

/**
 * Represents a shared lock.
 */
public interface SharedLock {

    /**
     * Gets the underlying {@link Lock}
     * @return
     */
    Lock getLock();

    /**
     * Locks this {@link SharedLock}, returning a {@link Monitor} which must be closed later.
     *
     * @return the {@link Monitor}
     */
    Monitor acquireMonitor();

}
