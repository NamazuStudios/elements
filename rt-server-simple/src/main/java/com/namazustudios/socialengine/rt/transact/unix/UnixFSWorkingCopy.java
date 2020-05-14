package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.exception.DuplicateException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.TransactionConflictException;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.namazustudios.socialengine.rt.id.ResourceId.randomResourceId;

/**
 * A working copy of the underlying values stored in the {@link UnixFSPathIndex}. This is used as a mirror of the
 * data so that the entry can properly compile a program to finalize the transaction.
 */
class UnixFSWorkingCopy {

    private static final ResourceId NULL_RESOURCE_ID = randomResourceId();

    private final Revision<?> revision;

    private final UnixFSPathIndex unixFSPathIndex;

    private final UnixFSOptimisticLocking optimisticLocking;

    private final HashMap<Path, ResourceId> paths = new HashMap<>();

    private final HashMap<ResourceId, Set<Path>> resourceIds = new HashMap<>();

    public UnixFSWorkingCopy(final Revision<?> revision,
                             final UnixFSPathIndex unixFSPathIndex,
                             final UnixFSOptimisticLocking optimisticLocking) {
        this.revision = revision;
        this.unixFSPathIndex = unixFSPathIndex;
        this.optimisticLocking = optimisticLocking;
    }

    public Set<Path> getPaths(final ResourceId resourceId) {
        Set<Path> paths = resourceIds.get(resourceId);
        if (paths == null) paths = load(resourceId);
        return paths;
    }

    public ResourceId getResourceId(final Path path) {
        ResourceId resourceId = paths.get(path);

        if (resourceId == null) {
            resourceId = unixFSPathIndex
                .getRevisionMap()
                .getValueAt(revision, path)
                .getValue().orElseThrow(ResourceNotFoundException::new);
            load(resourceId);
        }

        return resourceId;

    }

    private Set<Path> load(final ResourceId resourceId) {

        final Set<Path> paths = unixFSPathIndex
            .getReverseRevisionMap()
            .getValueAt(revision, resourceId)
            .getValue()
            .map(HashSet::new)
            .orElseThrow(ResourceNotFoundException::new);

        paths.forEach(path -> this.paths.put(path, resourceId));
        if (resourceIds.putIfAbsent(resourceId, paths) != null) throw new IllegalStateException("Already loaded");

        return paths;

    }

    public boolean isPresent(final ResourceId resourceId) {
        load(resourceId);
        return resourceIds.containsKey(resourceId);
    }

    public boolean isPresent(Path path) {
        return false;
    }

    public ResourceService.Unlink unlink(final Path path, final Runnable success) {
        return null;
    }

    public WritableByteChannel saveNewResource(
            final Path path,
            final ResourceId resourceId,
            final UnixFSUtils.IOOperation<WritableByteChannel> writableByteChannelSupplier) throws TransactionConflictException, IOException {

        // Checks that both paths and resource IDs are free and available for use

        if (isPresent(path)) throw new DuplicateException();
        if (isPresent(resourceId)) throw new DuplicateException();

        // Locks them to ensure that this transactional entry can access them.
        optimisticLocking.lock(path);
        optimisticLocking.lock(resourceId);

        return writableByteChannelSupplier.perform();

    }

    public void linkNewResource(final ResourceId sourceResourceId,
                                final Path destination,
                                final Runnable onSuccess) throws TransactionConflictException {
        if (isPresent(destination)) throw new DuplicateException();
        if (isPresent(sourceResourceId)) throw new DuplicateException();
        optimisticLocking.lock(destination);
        optimisticLocking.lock(sourceResourceId);
        onSuccess.run();
    }

    public void linkExistingResource(final ResourceId sourceResourceId,
                                     final Path destination,
                                     final Runnable onSuccess) throws TransactionConflictException {
        // Ensure that the path and resource ID exists, but the path is available for use.
        if (isPresent(destination)) throw new DuplicateException();
        if (!isPresent(sourceResourceId)) throw new ResourceNotFoundException();
        optimisticLocking.lock(destination);
        optimisticLocking.lock(sourceResourceId);
        onSuccess.run();
    }

    public List<ResourceService.Unlink> unlinkMultiple(
            final Path path,
            final int max,
            final UnlinkOperation unlinkOperation) throws TransactionConflictException {
        return null;
    }

    public void removeResource(final ResourceId resourceId, final Runnable onSuccess) throws TransactionConflictException {
        optimisticLocking.lock(resourceId);
        // TODO Implement
        onSuccess.run();
    }

    public List<ResourceId> removeResources(
            final Path path, final int max,
            final BiConsumer<Path, ResourceId> onRemove) throws TransactionConflictException {
        return null;
    }

    @FunctionalInterface
    public interface UnlinkOperation {

        void processRemoval(final Path fqPath, final ResourceId resourceId, final boolean delete);

    }

}
