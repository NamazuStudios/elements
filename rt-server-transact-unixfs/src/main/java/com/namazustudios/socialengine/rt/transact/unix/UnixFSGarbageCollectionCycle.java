package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgramInterpreter.ExecutionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.*;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.LinkType.REVISION_HARD_LINK;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSUtils.LinkType.REVISION_SYMBOLIC_LINK;
import static java.nio.file.Files.*;
import static java.util.Collections.reverseOrder;

public class UnixFSGarbageCollectionCycle {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSGarbageCollectionCycle.class);

    // Injected fields.

    private final UnixFSUtils utils;

    private final UnixFSRevisionPool unixFSRevisionPool;

    private final UnixFSTransactionJournal transactionJournal;

    private final UnixFSPessimisticLocking pessimisticLocking;

    // Variables used within the scope of the collection cycle

    private final List<Operation> collectOperations = new ArrayList<>();

    private final SortedMap<ResourceId, Operation> resourceIdOperations = new TreeMap<>(this::sort);

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

    public void collect(final List<UnixFSRevisionTableEntry> targetRevisionTableEntry) {

        collectOperations.clear();
        pathOperations.clear();
        resourceIdOperations.clear();

        try {

        } finally {

        }

    }

    private class MarkPass implements ExecutionHandler {

        private final UnixFSRevision<?> revision;

        public MarkPass(final UnixFSTransactionProgram program) {
            this.revision = unixFSRevisionPool.create(program.header.revision);
        }

        @Override
        public void unlinkRTPath(final UnixFSTransactionProgram program,
                                 UnixFSTransactionCommand command, final com.namazustudios.socialengine.rt.Path rtPath) {

            final NodeId nodeId = program.header.nodeId.get();
            final UnixFSPathMapping pathMapping = UnixFSPathMapping.fromRTPath(utils, nodeId, rtPath);
            final UnixFSRevision<?> revision = unixFSRevisionPool.create(program.header.revision);

            utils.findRevisionsUpTo(pathMapping.getPathDirectory(), revision, REVISION_SYMBOLIC_LINK)
                 .filter(r -> r.getValue().isPresent())
                 .forEach(r -> pathOperations.put(rtPath, () -> {

                     final Path path = r.getValue().get();
                     final Path directory = path.getParent();

                     logger.trace("Deleting file {} @ revision {}", path, r.getUniqueIdentifier());
                     deleteIfExists(path);

                     if (!list(directory).findAny().isPresent() && pessimisticLocking.tryLock(rtPath)) {
                         logger.trace("Directory {} is empty. Deleting.", directory);
                         deleteIfExists(directory);
                     }

                 }));

        }

        @Override
        public void removeResource(final UnixFSTransactionProgram program,
                                   final UnixFSTransactionCommand command,
                                   final ResourceId resourceId) {

            final UnixFSRevision<?> revision = unixFSRevisionPool.create(program.header.revision);
            final UnixFSResourceIdMapping resourceIdMapping = UnixFSResourceIdMapping.fromResourceId(utils, resourceId);

            logger.trace("Processing {}", command);

            utils.findRevisionsUpTo(resourceIdMapping.getResourceIdDirectory(), revision, REVISION_HARD_LINK)
                 .filter(r -> r.getValue().isPresent())
                 .forEach(r -> resourceIdOperations.put(resourceId, () -> {

                     final Path path = r.getValue().get();
                     final Path directory = path.getParent();

                     logger.trace("Deleting file {} @ revision {}", path, r.getUniqueIdentifier());
                     deleteIfExists(path);

                     if (!list(directory).findAny().isPresent() && pessimisticLocking.tryLock(resourceId)) {
                         logger.trace("Directory {} is empty. Deleting.", directory);
                         deleteIfExists(directory);
                     }

                 }));

        }

        @Override
        public Logger getLogger() {
            return logger;
        }

    }

    @FunctionalInterface
    private interface Operation { void perform() throws IOException; }

}
