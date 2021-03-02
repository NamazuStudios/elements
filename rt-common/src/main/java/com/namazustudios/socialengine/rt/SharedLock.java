package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.util.Monitor;

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
