package com.namazustudios.socialengine.rt;


import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.util.Monitor;

import java.util.concurrent.locks.Lock;

/**
 * Manages {@link Lock} instances for {@link Resource} instances.  Generally before manipulating a {@link Resource},
 * either direcly or through its {@link ResourceService} methods, it is necessary to acquire a {@link Lock} against
 * the {@link Resource}.  Note that this exists because a {@link Lock} may exist
 *
 * Created by patricktwohig on 4/11/17.
 */
public interface ResourceLockService {

    /**
     * Returns the number of {@link SharedLock} instances tracked in this {@link ResourceLockService}.
     *
     * @return the number of {@link SharedLock} instances
     */
    int size();

    /**
     * Returns a {@link SharedLock} for the provided {@link ResourceId}.
     *
     * @param resourceId the resource ID
     *
     * @return the {@link Monitor}
     */
    SharedLock getLock(ResourceId resourceId);

    /**
     * Returns a {@link Monitor} for the provided {@link ResourceId}.
     *
     * @param resourceId the resource ID
     *
     * @return the {@link Monitor}
     */
    default Monitor getMonitor(final ResourceId resourceId) {
        final var lock = getLock(resourceId);
        return lock.lock();
    }

    /**
     * Deletes the lock with the given {@link ResourceId}.
     */
    void delete(ResourceId resourceId);

}
