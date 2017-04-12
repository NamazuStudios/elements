package com.namazustudios.socialengine.rt;

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
     */
    Lock getLock(final ResourceId resourceId);

}
