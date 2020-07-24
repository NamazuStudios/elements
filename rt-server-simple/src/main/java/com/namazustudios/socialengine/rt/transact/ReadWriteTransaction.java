package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.ResourceService.Unlink;
import com.namazustudios.socialengine.rt.exception.DuplicateException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.stream.Stream;

/**
 * Defines a set of mutable transactional operations.
 */
public interface ReadWriteTransaction extends ReadOnlyTransaction {

    /**
     * Opens a {@link WritableByteChannel} to a newly defined {@link Resource} with the following {@link Path} and
     * {@link ResourceId}.
     *
     * If a {@link Path} is already occupied, or the {@link ResourceId} has already been added, then this throws an
     * instance of {@link DuplicateException}.
     *
     * @param path the path to link with the new {@link ResourceId}
     * @param resourceId the {@link ResourceId} of the newly inserted {@link ResourceId}
     * @return a {@link WritableByteChannel} which must be closed by the calling code when finished.
     *
     * @throws IOException if an {@link IOException} prevented opening the {@link WritableByteChannel}
     * @throws TransactionConflictException if another process is already attempting to modify the same {@link Path} or {@link ResourceId}
     */
    WritableByteChannel saveNewResource(final Path path, final ResourceId resourceId) throws IOException, TransactionConflictException;

    /***
     * Opens a {@link WritableByteChannel} to a newly defined {@link Resource} with the {@link ResourceId}
     *
     * If the {@link ResourceId} is not found, then this must throw an instance of {@link ResourceNotFoundException}
     * to indicate that the operation is not possible.
     *
     * @param resourceId the {@link ResourceId} of the existing {@link ResourceId}
     * @return a {@link WritableByteChannel} which must be closed by the calling code when finished.
     *
     * @throws IOException if an {@link IOException} prevented opening the {@link WritableByteChannel}
     * @throws ResourceNotFoundException if no {@link ResourceId} matches
     * @throws TransactionConflictException if another process is already attempting to modify the same {@link Path} or {@link ResourceId}
     */
    WritableByteChannel updateResource(final ResourceId resourceId) throws IOException, TransactionConflictException;

    /**
     * Creates a link betweeen a {@link Path} and {@link ResourceId}, provided that neither already exist. If either
     * exist, then this will throw an instance of {@link DuplicateException}.
     *
     * @param path the {@link Path} to link to the {@link ResourceId}
     * @param resourceId the {@link ResourceId} of the newly added {@link Resource}
     * @throws TransactionConflictException if another process is already attempting to modify the same {@link Path} or {@link ResourceId}
     */
    void linkNewResource(Path path, ResourceId resourceId) throws TransactionConflictException;

    /**
     * Links an existing {@link ResourceId} to a new {@link Path}, throwing an instance of
     * {@link ResourceNotFoundException} if the {@link ResourceId} is not known or throwing an instance of
     * {@link DuplicateException} if the supplied {@link Path} exists.
     *
     * @param sourceResourceId the {@link ResourceId} which will be the source of the link
     * @param destination the {@link Path} indicating the destination of the link
     * @throws TransactionConflictException if another process is already attempting to modify the same {@link Path} or {@link ResourceId}
     */
    void linkExistingResource(ResourceId sourceResourceId, Path destination) throws TransactionConflictException;

    /**
     * Unlinks the supplied {@link Path}, if the associated {@link ResourceId} is linked by no other {@link Path}s, then
     * this method will also delete the {@link ResourceId} associated with it. If there is not a resource for the
     * supplied {@link Path}, then this method will throw an instance of {@link ResourceNotFoundException}.
     *
     * The supplied {@link Path} must not be a wildcard.
     *
     * @param path the {@link Path} to unlink
     * @return an instance of {@link Unlink} indicating the result of the operation.
     * @throws TransactionConflictException if another process is already attempting to modify the supplied {@link Path}
     */
    Unlink unlinkPath(Path path) throws TransactionConflictException;

    /**
     * Unlinks the multiple {@link ResourceId}s associated with the supplied {@link Path}, if the associated
     * {@link ResourceId} is linked by no other {@link Path}s, then this method will also delete the {@link ResourceId}s
     * associated with ths {@link Path}.
     *
     * If the {@link Path} is not a wildcard {@link Path}, then the returned {@link Stream<Unlink>} will have, at most,
     * a single value.
     *
     * If the {@link Path} is a wildcard {@link Path}, then the returned {@link Stream<Unlink>} will have, at most, the
     * value specified in this method.
     *
     * This method should never throw an instance of {@link ResourceNotFoundException}, but rather return an empty
     * result if no {@link Path}s match.
     *
     * @param path the {@link Path} to unlink
     * @return an instance of {@link Unlink} indicating the result of the operation.
     * @throws TransactionConflictException if another process is already attempting to modify anything related to the supplied {@link Path}
     */
    List<Unlink> unlinkMultiple(Path path, int max) throws TransactionConflictException;

    /**
     * Removes the {@link Resource} with the supplied {@link ResourceId} and automatically removes any {@link Path}
     * instances that point to that {@link ResourceId}. If no such {@link ResourceId} exists, then this method must
     * throw an instance of {@link ResourceNotFoundException}.
     *
     * @param resourceId the {@link ResourceId} to remove
     * @throws TransactionConflictException if another process is already attempting to modify the supplied {@link ResourceId}
     *
     */
    void removeResource(ResourceId resourceId) throws TransactionConflictException;

    /**
     * Removes multiple {@link ResourceId}s associated with the supplied {@link Path}, if the associated
     * {@link ResourceId} is linked by no other {@link Path}s, then this method will also delete the {@link ResourceId}s
     * associated with ths {@link Path}.
     *
     * If the {@link Path} is not a wildcard {@link Path}, then the returned {@link Stream<ResourceId>} will have,
     * at most, a single value.
     *
     * If the {@link Path} is a wildcard {@link Path}, then the returned {@link Stream<ResourceId>} will have, at most,
     * the value specified in this method.
     *
     * This method should never throw an instance of {@link ResourceNotFoundException}, but rather return an empty
     * result if no {@link Path}s match.
     *
     * @param path the {@link Path} to unlink
     * @return an instance of {@link Unlink} indicating the result of the operation.
     * @throws TransactionConflictException if another process is already attempting to modify anything related to the supplied {@link Path}
     */
    List<ResourceId> removeResources(Path path, int max) throws TransactionConflictException;

    /**
     * Commits the {@link ReadWriteTransaction} to persistent storage. Once this method returns then all staged changes
     * will persist and can be visible to all new transactions.
     *
     * @throws TransactionConflictException
     */
    void commit() throws TransactionConflictException;

}
