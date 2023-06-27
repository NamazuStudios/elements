package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.Path;
import dev.getelements.elements.rt.id.ResourceId;

import java.util.Collection;

/**
 * Implements an optimistic locking approach. As the transaction is processed, lock each object of interest. If another
 * transaction locks those objects, then this will ensure that the second transaction will fail immediately upon
 * locking.
 *
 * Once the transaction is committed, the objects locked will be removed from the global locking pool. This allows
 * for multiple concurrent transactions writing unrelated data.
 */
public interface PessimisticLocking {

    /**
     * Attempts to lock the supplied {@link Path}, throwing an instance of {@link TransactionConflictException}
     * if another transaction is attempting to manipulate the same object.
     *
     * @param path the {@link Path} to lock
     * @throws TransactionConflictException if another process is manipulating the same {@link Path}
     */
    default void lock(Path path) throws TransactionConflictException {
        if (!tryLock(path)) throw new TransactionConflictException();
    }

    /**
     * Tries to lock the supplied {@link Path}, returning true if the lock operation succeeded.
     *
     * @param path the {@link Path} to lock
     * @return true if locked, false otherwise.
     */
    boolean tryLock(Path path);

    /**
     * Attempts to lock the supplied {@link ResourceId}, throwing an instance of
     * {@link TransactionConflictException} if another transaction is attempting to manipulate the same object.
     *
     * @param resourceId the {@link ResourceId} to lock
     * @throws TransactionConflictException if another process is manipulating the same {@link ResourceId}
     */
    default void lock(ResourceId resourceId) throws TransactionConflictException {
        if (!tryLock(resourceId)) throw new TransactionConflictException();
    }

    /**
     * Attempts to lock the supplied {@link ResourceId}, returning true if the lock operation succeeded.
     *
     * @param resourceId the {@link ResourceId} to lock
     * @return true if the operation succeeded, false otherwise
     */
    boolean tryLock(ResourceId resourceId);

    /**
     * Convenience method to lock multiple {@link Path} values
     *
     * @param values the values to lock
     * @throws TransactionConflictException if any of the supplied {@link Path}s are locked
     */
    default void lockPaths(Collection<? extends Path> values) throws TransactionConflictException {
        for (final Path value : values) lock(value);
    }

    /**
     * Releases all objects locked by this {@link PessimisticLocking} instance.
     */
    void unlock();

    /**
     * Unlocks a specific {@link Path}. If the {@link Path} was not previously locked, this will have no side effect.
     *
     * @param rtPath the {@link Path}
     * @return true if the value was originally locked
     */
    boolean unlock(Path rtPath);

    /**
     * Unlocks a specific {@link ResourceId}. if the {@link ResourceId} was not previously locked, this will have no
     * side effects.
     *
     * @param resourceId the {@link ResourceId}
     * @return true if the value was originally locked
     */
    boolean unlock(ResourceId resourceId);

}
