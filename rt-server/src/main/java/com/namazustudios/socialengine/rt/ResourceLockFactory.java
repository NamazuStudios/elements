package com.namazustudios.socialengine.rt;

/**
 * This is used by the {@link SimpleResourceService} to lock portions
 * of the resource graph.
 *
 * Created by patricktwohig on 8/22/15.
 */
public interface ResourceLockFactory<ResourceT extends Resource> {

    /**
     * Creates a lock resource.  A lock resource implements the ResourceT
     * type, with the following criteria.
     * <p/>
     * <ul>
     * <li>{@link #equals(Object)} evaluates using operator ==</li>
     * <li>{@link #hashCode()} evaluates using {@link System#identityHashCode(Object)}</li>
     * <li>All other should never be called.</li>
     * </ul>
     * <p/>
     * Lock objects are inserted in place of resources while the service moves
     * them around in memory.
     * <p/>
     * The default implementation of this method using CGLib to generate
     * a proxy meeting the requirements.  Methods other than {@link #hashCode()} and
     * {@link #equals(Object)} will proceed as normal but will log a warning if they
     * are called as this should never happen.
     *
     * When using the {@link Resource} obtained through this method, the {@link ResourceService}
     * implementation must invoke {@link Resource#close()} as it would any other
     * resource.
     *
     * @return the lock resource
     */
    ResourceT createLock();

    /**
     * Returns true if the given resource is a lock resource.  Passing in an instance
     * not returned by {@link #createLock()} is undefined behavior.
     *
     * @param resource the resource
     *
     * @return true if the resource is a lock
     */
    boolean isLock(ResourceT resource);

}
