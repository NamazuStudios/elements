package com.namazustudios.socialengine.rt;

/**
 * Provides a link to the {@link ResourceService} to perform certain operations from within a {@link Resource}.  THis
 * is essentially an internal implementation detail that allowes resources to acquire themselves and scheduleRelease themselves
 * for thigns such as pending network activity.
 */
public interface ResourceAcquisition {

    /**
     * Acquires the {@link Resource} with the ID.
     *
     * @param resourceId the {@link ResourceId}
     * @return the number of acquires
     */
    void acquire(ResourceId resourceId);

    /**
     * Releases the {@link Resource} with the ID.
     *
     * @param resourceId the {@link ResourceId}
     * @return the number of acquires
     */
    void scheduleRelease(ResourceId resourceId);

}
