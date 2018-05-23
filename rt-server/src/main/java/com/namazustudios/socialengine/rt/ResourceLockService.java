package com.namazustudios.socialengine.rt;


import com.namazustudios.socialengine.rt.exception.DuplicateException;

import java.util.concurrent.locks.Lock;

/**
 * Manages {@link Lock} instances for {@link Resource} instances.  This is necessary because a single {@link Resource}
 * may be exist in more than one instance as they can be persisted to disk.
 *
 * Created by patricktwohig on 4/11/17.
 */
public interface ResourceLockService {

    /**
     * Gets the {@link Lock} for the provided {@link ResourceId}
     *
     * @param resourceId the resource ID
     *
     * @return a {@link Lock} used to serialize access to the provided resource
     */
    Lock getLock(ResourceId resourceId);

    /**
     * Returns a {@link Monitor} for the provided {@link ResourceId}.
     *
     * @param resourceId the resource ID
     *
     * @return the {@link Monitor}
     */
    default Monitor getMonitor(final ResourceId resourceId) {
        final Lock lock = getLock(resourceId);
        lock.lock();
        return () -> lock.unlock();
    }

    /**
     * Deletes the lock with the given {@link ResourceId}.
     */
    void delete(ResourceId resourceId);

    /**
     * Convienience wrapper to automatically close a {@link Lock} managed by this instance.
     */
    interface Monitor extends AutoCloseable {

        /**
         * Releases the underlyin {@link Lock}
         */
        @Override
        void close();

    }

}
