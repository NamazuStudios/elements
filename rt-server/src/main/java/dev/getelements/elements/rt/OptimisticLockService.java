package dev.getelements.elements.rt;

/**
 * This is used by the some {@link ResourceService} instances to lock portions of the resource path mapping.  In order
 * to maintain consistency, instances returned by this service will be used as place hodlers for actual objects
 * temporarily while other operations will continually attempt to repeat the operation until it succeeds or it exceeds
 * the prescribed number of attempts.  In a well-designed application operations should rarely repeat or retry, hence
 * the term Optimistic.  However, in cases where there may be high contention of a single {@link Resource} an on-disk
 * implementation with transactional support may perform better.
 *
 * An OptimisticLock is a dummy implementation of another type which has two characteristics.  It will only ever be
 * equal to itself (using {@link #equals(Object)}) and can be tested for being a lock type using
 * {@link #isLock(Object)}.
 *
 * Not all implementations of {@link ResourceService} need an {@link OptimisticLockService} to function, especially
 * those that write the database to disk.
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
