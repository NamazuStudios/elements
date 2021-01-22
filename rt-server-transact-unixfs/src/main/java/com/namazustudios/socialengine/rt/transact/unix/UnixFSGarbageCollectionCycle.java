package com.namazustudios.socialengine.rt.transact.unix;

import com.google.common.base.Stopwatch;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgramInterpreter.ExecutionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.LinkType.*;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Collections.reverseOrder;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class UnixFSGarbageCollectionCycle {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSGarbageCollectionCycle.class);

    // Injected fields.

    private final UnixFSUtils utils;

    private final UnixFSRevisionPool unixFSRevisionPool;

    private final UnixFSTransactionJournal transactionJournal;

    private final UnixFSPessimisticLocking pessimisticLocking;

    // Variables used within the scope of the collection cycle

    private final List<Operation> uncategorizedOperations = new ArrayList<>();

    private final SortedMap<ResourceId, Operation> resourceOperations = new TreeMap<>(this::sort);

    private final SortedMap<ResourceId, Operation> reverseMappingOperations = new TreeMap<>(this::sort);

    private final SortedMap<com.namazustudios.socialengine.rt.Path, Operation> pathOperations = new TreeMap<>(reverseOrder(this::sort));

    @Inject
    public UnixFSGarbageCollectionCycle(final UnixFSUtils utils,
                                        final UnixFSRevisionPool unixFSRevisionPool,
                                        final UnixFSTransactionJournal transactionJournal) {
        this.utils = utils;
        this.unixFSRevisionPool = unixFSRevisionPool;
        this.transactionJournal = transactionJournal;
        this.pessimisticLocking = transactionJournal.newPessimisticLocking();
    }

    private int sort(final com.namazustudios.socialengine.rt.Path l, final com.namazustudios.socialengine.rt.Path r) {
        // We sort paths by their length (in components) and then lexicographically after that. This ensures that the
        // shortest paths are at the beginning of the collection.
        final int lengthDifference = l.getComponents().size() - r.getComponents().size();
        return lengthDifference == 0 ? l.toString().compareTo(r.toString()) : lengthDifference;
    }

    private int sort(final ResourceId l, final ResourceId r) {
        // Less important, but this sorts each ResourceId lexicographically
        return l.toString().compareTo(r.toString());
    }

    public void collect(final List<UnixFSRevisionTableEntry> revisionsToCollect) {

        final Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            logger.info("Starting Garbage Collection Cycle.");
            clear();
            revisionsToCollect.forEach(this::collect);
            pathOperations.values().stream().forEach(Operation::performNoThrow);
            resourceOperations.values().stream().forEach(Operation::performNoThrow);
            reverseMappingOperations.values().stream().forEach(Operation::performNoThrow);
            uncategorizedOperations.stream().forEach(Operation::performNoThrow);
            transactionJournal.reclaimInvalidEntries();
        } finally {
            clear();
            logger.info("Garbage collection finished in {}ms", stopwatch.elapsed(MILLISECONDS));
        }

    }

    private void collect(final UnixFSRevisionTableEntry entry) {
        final CollectionPassExecutionHandler handler = new CollectionPassExecutionHandler();
        final UnixFSRevision<?> revision = unixFSRevisionPool.create(entry.revision);
        transactionJournal.findValidProgramForRevision(revision).ifPresent(s -> interpret(handler, s));
    }

    private void interpret(final CollectionPassExecutionHandler collectionPassExecutionHandler,
                           final UnixFSCircularBlockBuffer.Slice<UnixFSTransactionProgram> slice) {

        final Stopwatch stopwatch = Stopwatch.createStarted();
        final UnixFSTransactionProgram program = slice.getValue();
        final UnixFSRevision<?> revision = unixFSRevisionPool.create(program.header.revision);
        logger.debug("Beginning pass for revision {}", revision);

        try {
            program.interpreter().executeCleanupPhase(collectionPassExecutionHandler);
            program.interpreter().executeCommitPhase(collectionPassExecutionHandler);
            uncategorizedOperations.add(() -> slice.clear());
        } finally {
            logger.debug("Pass completed for revision {} in {}ms", revision, stopwatch.elapsed(MILLISECONDS));
        }

    }

    private void clear() {
        pathOperations.clear();
        resourceOperations.clear();
        reverseMappingOperations.clear();
        uncategorizedOperations.clear();
        pessimisticLocking.unlock();
    }

    private class CollectionPassExecutionHandler implements ExecutionHandler {

        @Override
        public void unlinkRTPath(final UnixFSTransactionProgram program,
                                 final UnixFSTransactionCommand command,
                                 final ResourceId resourceId,
                                 final com.namazustudios.socialengine.rt.Path rtPath) {

            final NodeId nodeId = program.header.nodeId.get();
            final UnixFSPathMapping pathMapping = UnixFSPathMapping.fromRTPath(utils, nodeId, rtPath);
            final UnixFSRevision<?> revision = unixFSRevisionPool.create(program.header.revision);

            final Operation aggregateOperation = utils
                .findRevisionsUpTo(pathMapping.getPathDirectory(), revision, REVISION_SYMBOLIC_LINK)
                .sorted()
                .filter(r -> r.getValue().isPresent())
                .map(r -> (Operation) () -> {
                    final Path path = r.getValue().get();
                    logger.trace("Deleting file {} @ revision {}", path, revision.getUniqueIdentifier());
                    deleteIfExists(path);
                    collectPathDirectoryIfEmpty(pathMapping);
                })
                .reduce(Operation.initial(), Operation::andThen);

            collectAncestorDirectories(rtPath);
            pathOperations.put(rtPath, aggregateOperation);

        }

        private void collectAncestorDirectories(final com.namazustudios.socialengine.rt.Path rtPath) {
            for (com.namazustudios.socialengine.rt.Path parent = rtPath.parent(); !parent.isRoot(); parent = parent.parent()) {
                final NodeId nodeId = rtPath.getNodeId();
                final UnixFSPathMapping pathMapping = UnixFSPathMapping.fromRTPath(utils, nodeId, parent);
                pathOperations.put(parent, () -> collectPathDirectoryIfEmpty(pathMapping));
            }
        }

        private void collectPathDirectoryIfEmpty(final UnixFSPathMapping pathMapping) throws IOException {

            final Path directory = pathMapping.getPathDirectory();

            try {
                if (utils.list(directory).findAny().isPresent() && pessimisticLocking.tryLock(pathMapping.getPath())) {
                    // If the lock fails, it's because a transaction is writing the directory. In that case,
                    // if the directory is ever fully emptied it will be picked up in subsequent collection
                    // cycles.
                    //
                    // We use try lock because the garbage collector simply can't fail and re-process.
                    logger.trace("Directory {} is empty. Deleting.", directory);
                    deleteIfExists(directory);
                }
            } finally {
                pessimisticLocking.unlock(pathMapping.getPath());
            }

        }

        @Override
        public void removeResource(final UnixFSTransactionProgram program,
                                   final UnixFSTransactionCommand command,
                                   final ResourceId resourceId) {

            final UnixFSRevision<?> revision = unixFSRevisionPool.create(program.header.revision);
            final UnixFSResourceIdMapping resourceIdMapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);

            logger.trace("Processing {}", command);

            final Operation aggregateOperation = utils
                .findRevisionsUpTo(resourceIdMapping.getResourceIdDirectory(), revision, REVISION_HARD_LINK)
                .filter(r -> r.getValue().isPresent())
                .map(r -> (Operation) () -> {

                    final Path path = r.getValue().get();

                    try {
                        collectResource(resourceId, path, r);
                        collectReverse(program, resourceId);
                    } finally {
                        pessimisticLocking.unlock(resourceId);
                    }

                }).reduce(Operation.initial(), Operation::andThen);

            resourceOperations.put(resourceId, aggregateOperation);

        }

        private void collectResource(final ResourceId resourceId,
                                     final Path path,
                                     final Revision<?> revision) throws IOException {

            final Path directory = path.getParent();

            logger.trace("Deleting file {} @ revision {}", path, revision);
            if (!deleteIfExists(path)) logger.trace("{} does not exist.", path);

            if (utils.list(directory).findAny().isPresent() && pessimisticLocking.tryLock(resourceId)) {
                // There really shouldn't be any contention for the most part. It's not a realistic use case
                // to create and subsequently destroy a resource as they are intended to be one-time-use.
                // However, that wouldn't stop bad code from attempting to do that so we must treat this
                // case as if you would treat the removal of a directory.
                //
                // We use try lock because the garbage collector simply can't fail and re-process.
                logger.trace("Directory {} is empty. Deleting.", directory);
                if (!deleteIfExists(directory)) logger.trace("{} does not exist.", path);
            }

        }

        @Override
        public void unlinkFile(final UnixFSTransactionProgram program,
                               final UnixFSTransactionCommand command,
                               final Path fsPath) {
            uncategorizedOperations.add(() -> {
                if (deleteIfExists(fsPath)) logger.trace("Deleted {}", fsPath);
                else logger.trace("File does not exist {}. Skipping.", fsPath);
            });
        }

        private void collectReverse(final UnixFSTransactionProgram program,
                                    final ResourceId resourceId) {

            final NodeId nodeId = resourceId.getNodeId();
            final UnixFSReversePathMapping reversePathMapping = UnixFSReversePathMapping.fromNodeId(utils, nodeId);
            final Path directory = reversePathMapping.resolveReverseDirectory(resourceId);
            final UnixFSRevision<?> revision = unixFSRevisionPool.create(program.header.revision);

            final Operation aggregateOperation = utils
                .findRevisionsUpTo(directory, revision, DIRECTORY)
                .filter(r -> r.getValue().isPresent())
                .flatMap(r -> collectReverseSingleRevision(resourceId, r))
                .reduce(Operation.initial(), Operation::andThen);

            reverseMappingOperations.put(resourceId, aggregateOperation);

        }

        private Stream<Operation> collectReverseSingleRevision(final ResourceId resourceId,
                                                               final Revision<?> revision) {

            final NodeId nodeId = resourceId.getNodeId();
            final UnixFSReversePathMapping reversePathMapping = UnixFSReversePathMapping.fromNodeId(utils, nodeId);
            final Path directory = reversePathMapping.resolveDirectory(revision, resourceId);

            if (utils.isTombstone(directory)) return Stream.of(() -> deleteIfExists(directory));

            return utils.walk(directory).sorted(Comparator.reverseOrder()).map(item -> () -> {
                logger.trace("Deleting reverse mapping link {}", item);
                deleteIfExists(item);
            });

        }

        @Override
        public Logger getLogger() {
            return logger;
        }

    }

    @FunctionalInterface
    private interface Operation {

        void perform() throws IOException;

        default void performNoThrow() {
            try {
                perform();
            } catch (UncheckedIOException | IOException ex) {
                logger.error("Caught exception performing garbage collection operation.", ex);
            }
        }

        default Operation andThen(final Operation nextOperation) {
            return () -> {
                Operation.this.perform();
                nextOperation.perform();
            };
        }

        static Operation initial() {
            return () -> {};
        };

    }

}
