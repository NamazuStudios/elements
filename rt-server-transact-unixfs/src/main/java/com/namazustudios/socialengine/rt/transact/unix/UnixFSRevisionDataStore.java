package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.RevisionDataStore;
import com.namazustudios.socialengine.rt.transact.TransactionJournal;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSCircularBlockBuffer.Slice;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgramInterpreter.ExecutionHandler;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSRevisionTableEntry.State.COMMITTED;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSRevisionTableEntry.State.WRITING;
import static java.util.Comparator.comparing;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for tracking the current revision and providing access to the various indices that access the underlying
 * data. This implements the current revision using a table of revisions stored in a {@link UnixFSCircularBlockBuffer}
 * whereby pending operations are
 */
public class UnixFSRevisionDataStore implements RevisionDataStore {

    private static final Logger logger = getLogger(UnixFSRevisionDataStore.class);

    private UnixFSUtils utils;

    private UnixFSPathIndex pathIndex;

    private UnixFSResourceIndex resourceIndex;

    private UnixFSRevisionPool revisionPool;

    private UnixFSGarbageCollector garbageCollector;

    private UnixFSChecksumAlgorithm preferredChecksum;

    private UnixFSRevisionTable revisionTable;

    private UnixFSTransactionJournal transactionJournal;

    public void start() {
        recoverJournal();
    }

    private void recoverJournal() {

        logger.info("Recovering journal, if necessary.");

        final List<Slice<JournalRecoveryExecution>> executionList = getTransactionJournal()
            .entries()
            .map(s -> s.flatMap(bb -> new UnixFSTransactionProgram(bb, 0)))
            .filter(s -> s.getValue().isValid())
            .map(s -> s.flatMap(p -> p.interpreter()))
            .map(s -> s.flatMap(JournalRecoveryExecution::new))
            .collect(Collectors.toList());

        // We must execute every journal entry in the order of revision committed as this will be what is necessary
        // for each revision.
        executionList.sort(comparing(Slice::getValue));
        executionList.forEach(s -> {
            s.getValue().perform();
            s.clear();
        });

        logger.info("Recovered {} entries.", executionList.size());

    }

    public void stop() {}

    @Override
    public UnixFSPathIndex getPathIndex() {
        return pathIndex;
    }

    @Override
    public UnixFSResourceIndex getResourceIndex() {
        return resourceIndex;
    }

    @Override
    public LockedRevision lockLatestReadUncommitted() {

        final UnixFSRevisionTableEntry operation = getRevisionTable()
            .reverse()
            .map(slice -> slice.getValue())
            .filter(op -> op.isValid() && COMMITTED.equals(op.state.get()))
            .findFirst()
            .orElseThrow(FatalException::new);

        final UnixFSRevision<?> revision = getRevisionPool().create(operation.revision);

        return new LockedRevision() {

            @Override
            public Revision<?> getRevision() {
                return revision;
            }

            @Override
            public void close() {
                if (operation.readers.decrementAndGet() == 0) {
                    getGarbageCollector().hint(revision);
                }
            }

        };

    }

    @Override
    public PendingRevisionChange beginRevisionUpdate() {
        return new UnixFSPendingRevisionChange();
    }

    public PendingRevisionChange findPendingRevisionChange(final UnixFSRevision<?> revision) {
        return getRevisionTable()
            .reverse()
            .filter(s -> s.getValue().isValid() && WRITING.equals(s.getValue().state.get()))
            .filter(s -> {
                final UnixFSRevision<?> opRevision = getRevisionPool().create(s.getValue().revision);
                return revision.compareTo(opRevision) == 0;
            })
            .findFirst()
            .map(s -> new UnixFSPendingRevisionChange(s))
            .orElseThrow(FatalException::new);
    }

    @Override
    public Stream<ResourceId> removeAllResources() {
        // TODO Implement this. This should do a fast-nuke of the directory and remove all resources in the storage
        throw new UnsupportedOperationException();
    }

    public UnixFSUtils getUtils() {
        return utils;
    }

    @Inject
    public void setUtils(final UnixFSUtils utils) {
        this.utils = utils;
    }

    @Inject
    public void setPathIndex(final UnixFSPathIndex pathIndex) {
        this.pathIndex = pathIndex;
    }

    @Inject
    public void setResourceIndex(final UnixFSResourceIndex resourceIndex) {
        this.resourceIndex = resourceIndex;
    }

    public UnixFSRevisionPool getRevisionPool() {
        return revisionPool;
    }

    @Inject
    public void setRevisionPool(final UnixFSRevisionPool revisionPool) {
        this.revisionPool = revisionPool;
    }

    public UnixFSGarbageCollector getGarbageCollector() {
        return garbageCollector;
    }

    @Inject
    public void setGarbageCollector(final UnixFSGarbageCollector garbageCollector) {
        this.garbageCollector = garbageCollector;
    }

    public UnixFSChecksumAlgorithm getPreferredChecksum() {
        return preferredChecksum;
    }

    @Inject
    public void setPreferredChecksum(final UnixFSChecksumAlgorithm preferredChecksum) {
        this.preferredChecksum = preferredChecksum;
    }

    public UnixFSRevisionTable getRevisionTable() {
        return revisionTable;
    }

    @Inject
    public void setRevisionTable(final UnixFSRevisionTable revisionTable) {
        this.revisionTable = revisionTable;
    }

    public UnixFSTransactionJournal getTransactionJournal() {
        return transactionJournal;
    }

    @Inject
    public void setTransactionJournal(final UnixFSTransactionJournal transactionJournal) {
        this.transactionJournal = transactionJournal;
    }

    private ExecutionHandler newExecutionHandler(
            final NodeId nodeId,
            final Revision<?> revision) {
        return new ExecutionHandler() {

            @Override
            public void unlinkFile(final UnixFSTransactionProgram program, final Path fsPath) {
                getUtils().doOperationV(() -> Files.delete(fsPath), FatalException::new);
            }

            @Override
            public void unlinkRTPath(final UnixFSTransactionProgram program,
                                     final com.namazustudios.socialengine.rt.Path rtPath) {
                getPathIndex().unlink(revision, nodeId, rtPath);
            }

            @Override
            public void removeResource(final UnixFSTransactionProgram program, final ResourceId resourceId) {
                getResourceIndex().removeResource(revision, resourceId);
            }

            @Override
            public void updateResource(final UnixFSTransactionProgram program,
                                       final Path fsPath,
                                       final ResourceId resourceId) {
                getResourceIndex().updateResource(revision, fsPath, resourceId);
            }

            @Override
            public void addPath(final UnixFSTransactionProgram program,
                                final com.namazustudios.socialengine.rt.Path path) {
                getPathIndex().addPath(revision, path);
            }

            @Override
            public void addResourceId(final UnixFSTransactionProgram program, final ResourceId resourceId) {
                getResourceIndex().addResourceId(revision, resourceId);
            }

            @Override
            public void linkNewResource(final UnixFSTransactionProgram program,
                                        final Path fsPath,
                                        final ResourceId resourceId) {
                getResourceIndex().linkNewResource(revision, fsPath, resourceId);
            }

            @Override
            public void linkResourceToRTPath(final UnixFSTransactionProgram program,
                                             final ResourceId resourceId,
                                             final com.namazustudios.socialengine.rt.Path rtPath) {
                getPathIndex().link(revision, nodeId, resourceId, rtPath);
                getPathIndex().linkReverse(revision, nodeId, resourceId, rtPath);
            }

        };
    }

    private class UnixFSPendingRevisionChange implements PendingRevisionChange {

        private boolean open = true;

        private boolean destroy = false;

        private final UnixFSRevision<?> revision;

        private final UnixFSRevisionTableEntry tableEntry;

        private final Slice<UnixFSRevisionTableEntry> tableEntrySlice;

        private UnixFSPendingRevisionChange() {

            final UnixFSChecksumAlgorithm preferredAlgorithm = getPreferredChecksum();

            revision = getRevisionPool().createNextRevision();
            tableEntrySlice = getRevisionTable().nextLeading();

            tableEntry = tableEntrySlice.getValue();
            tableEntry.state.set(WRITING);
            tableEntry.revision.fromRevision(revision);
            tableEntry.algorithm.set(preferredAlgorithm);

            preferredAlgorithm.compute(tableEntry);

        }

        private UnixFSPendingRevisionChange(final Slice<UnixFSRevisionTableEntry> tableEntrySlice) {
            this.tableEntrySlice = tableEntrySlice;
            this.tableEntry = tableEntrySlice.getValue();
            this.revision = getRevisionPool().create(tableEntry.revision);
        }

        @Override
        public void update() {

            final UnixFSChecksumAlgorithm preferredAlgorithm = getPreferredChecksum();
            final Slice<UnixFSRevisionTableEntry> slice = revisionTable.nextLeading().clear();
            final UnixFSRevisionTableEntry op = slice.getValue();

            destroy = true;

            op.state.set(COMMITTED);
            op.revision.fromRevision(revision);
            op.algorithm.set(preferredAlgorithm);

            preferredAlgorithm.compute(op);

        }

        @Override
        public void apply(final TransactionJournal.Entry entry) {

            final UnixFSJournalEntry journalEntry = entry.getOriginal(UnixFSJournalEntry.class);
            final NodeId nodeId = journalEntry.getNodeId();
            final UnixFSTransactionProgram transactionProgram = journalEntry.getProgram();
            final UnixFSTransactionProgramInterpreter interpreter = transactionProgram.interpreter();
            final ExecutionHandler executionHandler = newExecutionHandler(nodeId, revision);

            try {
                interpreter.executeCommitPhase(executionHandler);
            } finally {
                interpreter.executeCleanupPhase(executionHandler);
            }

        }

        @Override
        public void cleanup(final TransactionJournal.Entry entry) {
            logger.warn("cleanup(Entry) not implemented yet.");
//            throw new FatalException("Not yet supported.");
        }

        @Override
        public void fail() {
            tableEntrySlice.clear();
        }

        @Override
        public Revision<?> getRevision() {
            return revision;
        }

        @Override
        public void close() {
            try {
                if (open && destroy) tableEntrySlice.clear();
            } finally {
                open = false;
            }
        }

    }

    private class JournalRecoveryExecution implements Comparable<JournalRecoveryExecution> {

        private final NodeId nodeId;

        private final UnixFSRevision<?> revision;

        private final UnixFSTransactionProgramInterpreter interpreter;

        private JournalRecoveryExecution(final UnixFSTransactionProgramInterpreter interpreter) {
            this.interpreter = interpreter;
            this.nodeId = interpreter.program.header.nodeId.get();
            this.revision = getRevisionPool().create(interpreter.program.header.revision);
        }

        @Override
        public int compareTo(JournalRecoveryExecution o) {
            return revision.compareTo(o.revision);
        }

        public void perform() {
            final ExecutionHandler executionHandler = newExecutionHandler(nodeId, revision);
            try (final LockedRevision lr = findPendingRevisionChange(revision)) {
                try {
                    interpreter.executeCommitPhase(executionHandler);
                } finally {
                    interpreter.executeCleanupPhase(executionHandler);
                }
            }
        }
    }

}
