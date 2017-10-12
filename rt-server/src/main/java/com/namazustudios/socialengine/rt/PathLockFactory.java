package com.namazustudios.socialengine.rt;

/**
 * This is used by the {@link SimpleResourceService} to lock portions of the resource path mapping.
 *
 * Created by patricktwohig on 8/22/15.
 */
public interface PathLockFactory {

    /**
     * Creates a lock {@link ResourceId} which is unique and otherwise unassociated with
     * an existing {@link ResourceId}.
     *
     * @return the lock resource
     */
    ResourceId createLock();

    /**
     * Returns true if the given resource is a lock resource.  Passing in an instance not returned by
     * {@link #createLock()} is undefined behavior.
     *
     * @param resource the {@link ResourceId}, may be null becuase "null" is not a lock (or anything for that matter)
     *
     * @return true if the resource is a lock
     */
    boolean isLock(ResourceId resource);

}
