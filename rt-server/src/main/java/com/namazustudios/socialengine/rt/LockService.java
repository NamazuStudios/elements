package com.namazustudios.socialengine.rt;


import com.namazustudios.socialengine.rt.exception.DuplicateException;

import java.util.concurrent.locks.Lock;

/**
 * Manages {@link Lock} instances for {@link Resource} instances.
 *
 * Created by patricktwohig on 4/11/17.
 */
public interface LockService {

    /**
     * Gets the {@link Lock} for the provided {@link ResourceId}
     *
     * @param resourceId the resource ID
     *
     * @return a {@link Lock} used to serialize access to the provided resource
     * @throws {@link DuplicateException} if no lock could be found for the given {@link ResourceId}
     */
    Lock getLock(final ResourceId resourceId);

//    /**
//     * Creates and returns the {@link Lock} instance provided the {@link ResourceId}.
//     *
//     * @param resourceId the resource ID
//     * @return the {@link Lock}
//     * @throws {@link DuplicateException} if a lock alrady exists for hte provide {@link ResourceId}
//     */
//    Lock create(final ResourceId resourceId);
//
//    /**
//     * Deletes the lock with the given {@link ResourceId}
//     */
//    void delete(final ResourceId);

}
