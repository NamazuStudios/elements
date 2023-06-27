package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.Path;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.exception.DuplicateException;
import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.id.ResourceId;
import dev.getelements.elements.rt.transact.Revision;
import dev.getelements.elements.rt.transact.TransactionConflictException;
import dev.getelements.elements.rt.transact.PessimisticLocking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * A working copy of the underlying values stored in the {@link UnixFSPathIndex}. This is used as a mirror of the
 * data so that the entry can properly compile a program to finalize the transaction.
 */
class UnixFSWorkingCopy {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSWorkingCopy.class);

    private final NodeId nodeId;

    private final Revision<?> revision;

    private final UnixFSPathIndex unixFSPathIndex;

    private final PessimisticLocking pessimisticLocking;

    private final Map<Object, Modification> modifications = new HashMap<>();

    public UnixFSWorkingCopy(final NodeId nodeId,
                             final Revision<?> revision,
                             final UnixFSPathIndex unixFSPathIndex,
                             final PessimisticLocking pessimisticLocking) {
        this.nodeId = nodeId;
        this.revision = revision;
        this.unixFSPathIndex = unixFSPathIndex;
        this.pessimisticLocking = pessimisticLocking;
    }

    private Modification get(final Path path) {
        requireNonNull(path, "path");
        return modifications.compute(path, (p, old) -> old == null ? load(path) : old).reindex();
    }

    private Modification load(final Path path) {

        final var resourceId = unixFSPathIndex
            .getRevisionMap(nodeId)
            .getValueAt(revision, path)
            .getValue()
            .orElse(null);

        final var paths = resourceId == null ? new HashSet<>(singleton(path)) : unixFSPathIndex
            .getReverseRevisionMap(nodeId)
            .getValueAt(revision, resourceId)
            .getValue()
            .orElseGet(() -> new HashSet<>(singleton(path)));

        return new Modification(resourceId, paths);

    }

    private Modification get(final ResourceId resourceId) {
        requireNonNull(resourceId, "resourceId");
        return modifications.compute(resourceId, (r, old) -> old == null ? load(resourceId) : old);
    }

    private Modification load(final ResourceId resourceId) {

        final Set<Path> paths = unixFSPathIndex
            .getReverseRevisionMap(nodeId)
            .getValueAt(revision, resourceId)
            .getValue()
            .orElseGet(HashSet::new);

        return new Modification(resourceId, paths);

    }

    public ResourceService.Unlink unlink(final Path path, final BiConsumer<ResourceId, Boolean> success) throws TransactionConflictException {

        final var modification = get(path).enforceExistence();

        pessimisticLocking.lock(path);
        pessimisticLocking.lock(modification.resourceId);

        modification.remove(path);

        final var removed = modification.isEmpty();
        success.accept(modification.resourceId, removed);

        return new ResourceService.Unlink() {

            @Override
            public ResourceId getResourceId() {
                return modification.resourceId;
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

        final var pathMod = get(path);
        final var resourceIdMod = get(resourceId);

        // Checks that both paths and resource IDs are free and available for use
        pathMod.checkDuplicate();
        resourceIdMod.checkDuplicate();

        // Locks them to ensure that this transactional entry can access them.
        pessimisticLocking.lock(path);
        pessimisticLocking.lock(resourceId);

        // Performs the linkage to the resource ID and the path.
        resourceIdMod.merge(pathMod).reindex();

        return writableByteChannelSupplier.perform();

    }

    public WritableByteChannel updateResource(
            final ResourceId resourceId,
            final UnixFSUtils.IOOperation<WritableByteChannel> writableByteChannelSupplier)
            throws TransactionConflictException, IOException {
        get(resourceId).enforceExistence();
        pessimisticLocking.lock(resourceId);
        return writableByteChannelSupplier.perform();
    }

    public void linkNewResource(final ResourceId resourceId,
                                final Path path,
                                final Runnable onSuccess) throws TransactionConflictException {

        // Checks that both paths and resource IDs are free and available for use

        final var pathMod = get(path).checkDuplicate();
        final var resourceIdMod = get(resourceId).checkDuplicate();

        // Locks them to ensure that this transactional entry can access them.
        pessimisticLocking.lock(path);
        pessimisticLocking.lock(resourceId);

        // Performs the linkage to the resource ID and the path.
        resourceIdMod.merge(pathMod).reindex();
        onSuccess.run();

    }

    public void linkExistingResource(final ResourceId resourceId,
                                     final Path path,
                                     final Runnable onSuccess) throws TransactionConflictException {

        final var pathMod = get(path).checkDuplicate();
        final var resourceIdMod = get(resourceId).enforceExistence();

        final Modification modification = get(resourceId);

        // Locks them to ensure that this transactional entry can access them.
        pessimisticLocking.lock(path);
        pessimisticLocking.lock(resourceId);

        // Performs the linkage to the resource ID and the path.
        resourceIdMod.merge(pathMod).reindex();
        onSuccess.run();

    }

    public List<ResourceService.Unlink> unlinkMultiple(
            final Path path, final int max,
            final UnlinkOperation unlinkOperation) throws TransactionConflictException {

        pessimisticLocking.lock(path);

        final var listings = list(path).limit(max).collect(toList());

        for (final var listing : listings) {
            // Lock Everything we want to clear out
            pessimisticLocking.lock(listing.getPath());
            pessimisticLocking.lock(listing.getResourceId());
        }

        return listings
            .stream()
            .map(listing -> {

                final var modification = get(listing.getResourceId());
                modification.remove(listing.getPath());

                final var removed = modification.paths.isEmpty();
                unlinkOperation.processRemoval(listing.getPath(), listing.getResourceId(), removed);

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
        final var resourceIdMod = get(resourceId).enforceExistence();
        pessimisticLocking.lock(resourceId);
        pessimisticLocking.lockPaths(resourceIdMod.paths);
        resourceIdMod.clear(onRemove);
    }

    public List<ResourceId>  removeResources(
            final Path path, final int max,
            final BiConsumer<Path, ResourceId> onRemove) throws TransactionConflictException {

        final var listings = list(path).limit(max).collect(toList());

        for (final ResourceService.Listing listing : listings) {
            // Lock Everything we want to clear out
            pessimisticLocking.lock(listing.getPath());
            pessimisticLocking.lock(listing.getResourceId());
        }

        final List<ResourceId> removed = new ArrayList<>();

        for (final var listing : listings) {

            final var resourceId = listing.getResourceId();
            final var modification = get(listing.getResourceId());

            if (modification.isPresent()) {
                removed.add(resourceId);
                modification.clear(p -> onRemove.accept(p, resourceId));
            }

        }

        return removed;

    }

    private final Stream<ResourceService.Listing> list(final Path path) {

        final Predicate<ResourceService.Listing> filter = l -> {
            final var mod = modifications.get(l.getPath());
            return mod != null && !mod.isEmpty();
        };

        return unixFSPathIndex.list(nodeId, revision, path)
            .getValue()
            .map(s -> s.filter(filter))
            .orElseGet(Stream::empty);

    }

    @FunctionalInterface
    public interface UnlinkOperation {

        void processRemoval(final Path fqPath, final ResourceId resourceId, final boolean delete);

    }

    private class Modification {

        private ResourceId resourceId;

        private final Set<Path> paths;

        public Modification(final Path path) {
            this(null, new HashSet<>(singleton(path)));
        }

        public Modification(final ResourceId resourceId, final Set<Path> paths) {
            this.paths = paths;
            this.resourceId = resourceId;
        }

        @Override
        public String toString() {
            return "Modification " + resourceId +
                    " -> " +
                   "{" + paths.stream().map(p -> p.toAbsolutePathString()).collect(joining()) + "}";
        }

        private boolean isEmpty() {
            return resourceId == null || paths.isEmpty();
        }

        private boolean isPresent() {
            return !isEmpty();
        }

        private Modification reindex() {
            reindexPaths();
            reindexResourceId();
            return this;
        }

        private Modification reindexPaths() {
            paths.forEach(p -> modifications.put(p, this));
            return this;
        }

        private Modification reindexResourceId() {
            if (resourceId != null) modifications.put(resourceId, this);
            return this;
        }

        public Modification merge(Modification other) {

            final var paths = new HashSet<Path>();
            paths.addAll(this.paths);
            paths.addAll(other.paths);

            final var resourceId =
                this.resourceId != null && other.resourceId == null ? this.resourceId :
                this.resourceId == null && other.resourceId != null ? other.resourceId :
                null;

            if (paths.isEmpty()) throw new IllegalArgumentException("Must have at least one path.");
            if (resourceId == null) throw new IllegalArgumentException("Must have at least one resourceId.");

            return new Modification(resourceId, paths);

        }

        public Modification checkDuplicate() {
            if (isPresent()) throw new DuplicateException();
            return this;
        }

        private Modification enforceExistence() {
            if (isEmpty()) throw new ResourceNotFoundException();
            return this;
        }

        public void clear(final Consumer<Path> onRemove) {
            paths.forEach(onRemove.andThen(p -> modifications.put(p, new Modification(p))));
        }

        private void remove(final Path path) {
            paths.remove(path);
            modifications.put(path, new Modification(path));
        }

    }

}
