package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.exception.DuplicateException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.TransactionConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.id.ResourceId.randomResourceId;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * A working copy of the underlying values stored in the {@link UnixFSPathIndex}. This is used as a mirror of the
 * data so that the entry can properly compile a program to finalize the transaction.
 */
class UnixFSWorkingCopy {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSWorkingCopy.class);

    private static final ResourceId NULL_RESOURCE_ID = randomResourceId();

    private final NodeId nodeId;

    private final Revision<?> revision;

    private final UnixFSPathIndex unixFSPathIndex;

    private final UnixFSPessimisticLocking pessimisticLocking;

    private final HashMap<Path, ResourceId> pathToResourceIds = new HashMap<>();

    private final HashMap<ResourceId, Set<Path>> resourceIdToPaths = new HashMap<>();

    public UnixFSWorkingCopy(final NodeId nodeId,
                             final Revision<?> revision,
                             final UnixFSPathIndex unixFSPathIndex,
                             final UnixFSPessimisticLocking pessimisticLocking) {
        this.nodeId = nodeId;
        this.revision = revision;
        this.unixFSPathIndex = unixFSPathIndex;
        this.pessimisticLocking = pessimisticLocking;
    }

    private ResourceId getResourceId(final Path path) {
        requireNonNull(path, "path");
        return pathToResourceIds.compute(path, (p, rid) -> rid == null ? load(path) : rid);
    }

    private ResourceId load(final Path path) {

        if (path.isWildcard()) throw new IllegalArgumentException("Must be regular path.");

        final ResourceId resourceId = unixFSPathIndex
                .getRevisionMap(nodeId)
                .getValueAt(revision, path)
                .getValue()
            .orElse(NULL_RESOURCE_ID);

        load(resourceId);
        return resourceId;

    }

    private Set<Path> getPathsForResourceId(final ResourceId resourceId) {
        requireNonNull(resourceId, "resourceId");
        return resourceIdToPaths.compute(resourceId, (rid, p) -> p == null ? load(rid) : p);
    }

    private Set<Path> load(final ResourceId resourceId) {

        if (resourceId.equals(NULL_RESOURCE_ID)) return emptySet();

        final Set<Path> paths = unixFSPathIndex
                .getReverseRevisionMap(nodeId)
                .getValueAt(revision, resourceId)
                .getValue()
                .map(HashSet::new)
            .orElseGet(HashSet::new);

        paths.forEach(path -> this.pathToResourceIds.put(path, resourceId));
        if (this.resourceIdToPaths.putIfAbsent(resourceId, paths) != null) throw new IllegalStateException("Already loaded");

        return paths;

    }

    private boolean isPresent(final ResourceId resourceId) {
        return !getPathsForResourceId(resourceId).isEmpty();
    }

    private boolean isPresent(final Path path) {
        return !NULL_RESOURCE_ID.equals(getResourceId(path));
    }

    public ResourceService.Unlink unlink(final Path path, final Runnable success) throws TransactionConflictException {

        final ResourceId resourceId = getResourceId(path);
        if (NULL_RESOURCE_ID.equals(resourceId)) throw new ResourceNotFoundException();

        pessimisticLocking.lock(path);
        pessimisticLocking.lock(resourceId);

        final Set<Path> paths = getPathsForResourceId(resourceId);
        if (paths.remove(path)) logger.warn("Consistency error. Reverse mapping broken.");

        final boolean removed = paths.isEmpty();
        resourceIdToPaths.put(resourceId, new HashSet<>());
        pathToResourceIds.put(path, NULL_RESOURCE_ID);

        success.run();

        return new ResourceService.Unlink() {

            @Override
            public ResourceId getResourceId() {
                return resourceId;
            }

            @Override
            public boolean isRemoved() {
                return removed;
            }

        };

    }

    public WritableByteChannel saveNewResource(
            final Path path,
            final ResourceId resourceId,
            final UnixFSUtils.IOOperation<WritableByteChannel> writableByteChannelSupplier)
            throws TransactionConflictException, IOException {

        // Checks that both paths and resource IDs are free and available for use

        if (isPresent(path))
            throw new DuplicateException();
        if (isPresent(resourceId))
            throw new DuplicateException();

        // Locks them to ensure that this transactional entry can access them.
        pessimisticLocking.lock(path);
        pessimisticLocking.lock(resourceId);

        // Performs the linkage to the resource ID and the path.
        pathToResourceIds.put(path, resourceId);
        getPathsForResourceId(resourceId).add(path);

        return writableByteChannelSupplier.perform();

    }

    public WritableByteChannel updateResource(
            final ResourceId resourceId,
            final UnixFSUtils.IOOperation<WritableByteChannel> writableByteChannelSupplier)
            throws TransactionConflictException, IOException {
        if (!isPresent(resourceId)) throw new ResourceNotFoundException();
        pessimisticLocking.lock(resourceId);
        return writableByteChannelSupplier.perform();
    }

    public void linkNewResource(final ResourceId resourceId,
                                final Path path,
                                final Runnable onSuccess) throws TransactionConflictException {

        // Checks that both paths and resource IDs are free and available for use

        if (isPresent(path))
            throw new DuplicateException();
        if (isPresent(resourceId))
            throw new DuplicateException();

        // Locks them to ensure that this transactional entry can access them.
        pessimisticLocking.lock(path);
        pessimisticLocking.lock(resourceId);

        // Performs the linkage to the resource ID and the path.
        pathToResourceIds.put(path, resourceId);
        getPathsForResourceId(resourceId).add(path);

        onSuccess.run();

    }

    private static Map<Object, Object> mm = new ConcurrentHashMap<>();

    public void linkExistingResource(final ResourceId resourceId,
                                     final Path path,
                                     final Runnable onSuccess) throws TransactionConflictException {
        // Checks that both paths and resource IDs are free and available for use

        if (mm.put(path, path) == null) {
            logger.warn("No dup.");
        } else {
            logger.warn("Found the dup.");
        }

        if (isPresent(path))
            throw new DuplicateException();
        if (!isPresent(resourceId))
            throw new ResourceNotFoundException();

        // Locks them to ensure that this transactional entry can access them.
        pessimisticLocking.lock(path);
        pessimisticLocking.lock(resourceId);

        // Performs the linkage to the resource ID and the path.
        pathToResourceIds.put(path, resourceId);
        getPathsForResourceId(resourceId).add(path);

        onSuccess.run();

    }

    public List<ResourceService.Unlink> unlinkMultiple(
            final Path path, final int max,
            final UnlinkOperation unlinkOperation) throws TransactionConflictException {

        final List<ResourceService.Listing> listings = unixFSPathIndex.list(nodeId, revision, path)
            .getValue()
            .orElseGet(Stream::empty)
            .limit(max)
            .collect(toList());

        for (final ResourceService.Listing listing : listings) {
            // Lock Everything we want to clear out
            pessimisticLocking.lock(listing.getPath());
            pessimisticLocking.lock(listing.getResourceId());
        }

        return listings
            .stream()
            .map(listing -> {

                final Set<Path> paths = getPathsForResourceId(listing.getResourceId());

                pathToResourceIds.put(listing.getPath(), NULL_RESOURCE_ID);
                if (!paths.remove(paths)) logger.warn("Consistency Error. Reverse path mapping broken.");

                final boolean removed = paths.isEmpty();
                unlinkOperation.processRemoval(listing.getPath(), listing.getResourceId(), paths.isEmpty());

                return new ResourceService.Unlink() {

                    @Override
                    public ResourceId getResourceId() {
                        return listing.getResourceId();
                    }

                    @Override
                    public boolean isRemoved() {
                        return removed;
                    }

                };

            }).collect(toList());

    }

    public void removeResource(final ResourceId resourceId,
                               final Consumer<Path> onRemove) throws TransactionConflictException {

        if (!isPresent(resourceId)) throw new ResourceNotFoundException();
        final Set<Path> paths = getPathsForResourceId(resourceId);

        pessimisticLocking.lock(resourceId);
        pessimisticLocking.lockPaths(paths);

        doRemove(paths, onRemove);

    }

    public List<ResourceId> removeResources(
            final Path path, final int max,
            final BiConsumer<Path, ResourceId> onRemove) throws TransactionConflictException {

        final List<ResourceService.Listing> listings = unixFSPathIndex.list(nodeId, revision, path)
                .getValue()
                .orElseGet(Stream::empty)
                .limit(max)
            .collect(toList());

        for (final ResourceService.Listing listing : listings) {
            // Lock Everything we want to clear out
            pessimisticLocking.lock(listing.getPath());
            pessimisticLocking.lock(listing.getResourceId());
        }

        return listings
            .stream()
            .map(listing -> {
                final ResourceId resourceId = listing.getResourceId();
                final Set<Path> paths = getPathsForResourceId(resourceId);
                doRemove(paths, p -> onRemove.accept(p, resourceId));
                return listing.getResourceId();
            }).collect(toList());

    }

    private void doRemove(final Set<Path> paths, final Consumer<Path> onRemove) {

        final Set<Path> removed = new HashSet<>();

        try {
            paths.forEach(onRemove.andThen(removed::add));
        } finally {
            paths.removeAll(removed);
            pathToResourceIds.keySet().removeAll(removed);
        }

    }

    @FunctionalInterface
    public interface UnlinkOperation {

        void processRemoval(final Path fqPath, final ResourceId resourceId, final boolean delete);

    }

}
