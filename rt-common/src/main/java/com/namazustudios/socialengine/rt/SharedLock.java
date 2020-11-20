package com.namazustudios.socialengine.rt;

import java.util.concurrent.locks.Lock;

/**
 * Represents a shared lock.
 */
public interface SharedLock {

    /**
     * Locks this {@link SharedLock}, returning a {@link Monitor} which must be closed later.
     *
     * @return the {@link Monitor}
     */
    Monitor lock();

}
