package com.namazustudios.socialengine.rt;

/**
 * This is used by the {@link SimpleResourceService} to lock portions of the resource path mapping.  The
 * {@link SimpleResourceService} uses optimistic locking to maintain consistency.  In order to maintain consistency, the
 * {@link SimpleResourceService} will insert placeholders into its internal structures while performing operations.  If
 * a second thread attempts to manipulate these structures at the same time, it checks that the instances aren't lock
 * types.  If it finds a lock, it re-attempts after some fall-back time.
 *
 * An OptimisticLock is a dummy implementation of another type which has two characteristics.  It will only ever be
 * equal to itself (using {@link #equals(Object)}) and can be tested for being a lock type using {@link #isLock(Object)}.
 *
 * Created by patricktwohig on 8/22/15.
 */
public interface OptimisticLockService<LockT> {

    /**
     * Creates a lock which is unique and otherwise unassociated with an existing {@link LockT}.
     *
     * @return the lock resource
     */
    LockT createLock();

    /**
     * Returns true if the given resource is a lock resource.  Passing in an instance not returned by this instance's
     * {@link #createLock()} is undefined behavior.  This method must accept null, in which case, the return value must
     * be false.
     *
     * @param candidate the LockT candidate, or null.
     *
     * @return true if the resource is a lock
     */
    boolean isLock(LockT candidate);

}
