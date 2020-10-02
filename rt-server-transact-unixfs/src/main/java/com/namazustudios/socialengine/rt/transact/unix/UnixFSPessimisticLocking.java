package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.TransactionConflictException;

import java.util.Collection;

/**
 * Implements an optimistic locking approach. As the transaction is processed, the {@link UnixFSJournalEntry} will
 * lock each object of interest. If another transaction locks those objects, then this will ensure that the second
 * transaction will fail immediately upon locking.
 *
 * Once the transaction is committed, the objects locked will be removed from the global locking pool. This allows
 * for multiple concurrent transactions writing unrelated data.
 */
public interface UnixFSPessimisticLocking {

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
     * Convenience method to lock multiple {@link ResourceId} values
     *
     * @param values the values to lock
     * @throws TransactionConflictException if any of the supplied {@link ResourceId}s are locked
     */
    default void lockResources(Collection<? extends ResourceId> values) throws TransactionConflictException {
        for (final ResourceId value : values) lock(value);
    }

    /**
     * Releases all objects locked by this {@link UnixFSPessimisticLocking} instance.
     */
    void unlock();

}
