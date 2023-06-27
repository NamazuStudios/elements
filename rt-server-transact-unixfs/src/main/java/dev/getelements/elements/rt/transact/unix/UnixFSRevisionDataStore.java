package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.id.ResourceId;
import dev.getelements.elements.rt.transact.FatalException;
import dev.getelements.elements.rt.transact.Revision;
import dev.getelements.elements.rt.transact.RevisionDataStore;
import dev.getelements.elements.rt.transact.TransactionJournal;
import dev.getelements.elements.rt.transact.unix.UnixFSCircularBlockBuffer.Slice;
import dev.getelements.elements.rt.transact.unix.UnixFSTransactionProgramInterpreter.ExecutionHandler;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static dev.getelements.elements.rt.transact.unix.UnixFSRevisionTableEntry.State.*;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for tracking the current revision and providing access to the various indices that access the underlying
 * data. This implements the current revision using a table of revisions stored in a {@link UnixFSCircularBlockBuffer}
 * whereby pending operations are
 */
public class UnixFSRevisionDataStore implements RevisionDataStore {

    private static final int REVISION_COLLECTION_THRESHOLD = 25;

    private static final Logger logger = getLogger(UnixFSRevisionDataStore.class);

    private UnixFSUtils utils;

    private UnixFSPathIndex pathIndex;

    private UnixFSResourceIndex resourceIndex;

    private UnixFSRevisionPool revisionPool;

    private UnixFSChecksumAlgorithm preferredChecksum;

    private UnixFSRevisionTable revisionTable;

    private UnixFSTransactionJournal transactionJournal;

    private UnixFSGarbageCollector garbageCollector;

    public void start() {
        recoverJournal();
    }

    private void recoverJournal() {

        logger.info("Recovering journal, if necessary.");

        final List<Slice<JournalRecoveryExecution>> executionList = getTransactionJournal()
            .validPrograms()
            .filter(s -> s.getValue().isValid())
            .map(s -> s.map(UnixFSTransactionProgram::interpreter))
            .map(s -> s.map(JournalRecoveryExecution::new))
            .collect(toList());

        // We must execute every journal entry in the order of revision committed as this will be what is necessary
        // for each revision.
        executionList.sort(comparing(Slice::getValue));
        executionList.forEach(s -> s.getValue().perform());

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
    public LockedRevision lockLatestReadCommitted() {

        final UnixFSRevisionTable.RevisionMonitor<Slice<UnixFSRevisionTableEntry>> monitor;
        monitor = getRevisionTable().readLockLatestReadCommitted();

        final UnixFSRevision<?> revision = getRevisionPool().create(monitor.getScope().getValue().revision);

        return new LockedRevision() {

            @Override
            public Revision<?> getRevision() {
                return revision;
            }

            @Override
            public void close() {
                monitor.close();
            }

        };

    }

    @Override
    public PendingRevisionChange beginRevisionUpdate() {
        return new UnixFSPendingRevisionChange();
    }

    public PendingRevisionChange lockEntryWithRevision(final UnixFSRevision<?> revision) {
        return new UnixFSPendingRevisionChange();
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

    public UnixFSGarbageCollector getGarbageCollector() {
        return garbageCollector;
    }

    @Inject
    public void setGarbageCollector(UnixFSGarbageCollector garbageCollector) {
        this.garbageCollector = garbageCollector;
    }

    private ExecutionHandler newExecutionHandler(
            final NodeId nodeId,
            final Revision<?> revision) {
        return new ExecutionHandler() {

            @Override
            public void unlinkFile(final UnixFSTransactionProgram program,
                                   final UnixFSTransactionCommand command,
                                   final Path fsPath) {
                getUtils().doOperationV(() -> deleteIfExists(fsPath), FatalException::new);
            }

            @Override
            public void unlinkRTPath(final UnixFSTransactionProgram program,
                                     final UnixFSTransactionCommand command,
                                     final ResourceId resourceId,
                                     final dev.getelements.elements.rt.Path rtPath) {
                getPathIndex().unlink(revision, nodeId, resourceId, rtPath);
                getPathIndex().unlinkReverse(revision, nodeId, resourceId, rtPath);
            }

            @Override
            public void removeResource(final UnixFSTransactionProgram program,
                                       final UnixFSTransactionCommand command,
                                       final ResourceId resourceId) {
                getResourceIndex().removeResource(revision, resourceId);
                getResourceIndex().removeResourceReverseMappings(revision, resourceId);
            }

            @Override
            public void updateResource(final UnixFSTransactionProgram program,
                                       final UnixFSTransactionCommand command,
                                       final Path fsPath,
                                       final ResourceId resourceId) {
                getResourceIndex().updateResource(revision, fsPath, resourceId);
            }

            @Override
            public void addPath(final UnixFSTransactionProgram program,
                                final UnixFSTransactionCommand command,
                                final dev.getelements.elements.rt.Path path) {
                getPathIndex().addPath(revision, path);
            }

            @Override
            public void addResourceId(final UnixFSTransactionProgram program,
                                      final UnixFSTransactionCommand command,
                                      final ResourceId resourceId) {
                getResourceIndex().addResourceId(revision, resourceId);
            }

            @Override
            public void linkNewResource(final UnixFSTransactionProgram program,
                                        final UnixFSTransactionCommand command,
                                        final Path fsPath,
                                        final ResourceId resourceId) {
                getResourceIndex().linkNewResource(revision, fsPath, resourceId);
            }

            @Override
            public void linkResourceToRTPath(final UnixFSTransactionProgram program,
                                             final UnixFSTransactionCommand command,
                                             final ResourceId resourceId,
                                             final dev.getelements.elements.rt.Path rtPath) {
                getPathIndex().link(revision, nodeId, resourceId, rtPath);
                getPathIndex().linkReverse(revision, nodeId, resourceId, rtPath);
            }

        };
    }

    private class UnixFSPendingRevisionChange implements PendingRevisionChange {

        private boolean open = true;

        private final UnixFSRevision<?> revision = getRevisionPool().createNextRevision();

        private final UnixFSRevisionTable.RevisionMonitor<Slice<UnixFSRevisionTableEntry>> monitor = getRevisionTable().writeLockNextLeading();

        private UnixFSPendingRevisionChange() {
            final UnixFSChecksumAlgorithm preferredAlgorithm = getPreferredChecksum();
            final UnixFSRevisionTableEntry tableEntry = monitor.getScope().getValue();
            tableEntry.state.set(WRITING);
            tableEntry.revision.fromRevision(revision);
            tableEntry.algorithm.set(preferredAlgorithm);
            preferredAlgorithm.compute(tableEntry);
        }

        @Override
        public void update() {
            if (!open) throw new IllegalStateException();
            final UnixFSChecksumAlgorithm preferredAlgorithm = getPreferredChecksum();
            final UnixFSRevisionTableEntry tableEntry = monitor.getScope().getValue();
            tableEntry.state.set(COMMITTED);
            tableEntry.algorithm.set(preferredAlgorithm);
            preferredAlgorithm.compute(tableEntry);
            revisionTable.updateReadCommitted(monitor.getScope());
        }

        @Override
        public void apply(final TransactionJournal.Entry entry) {
            check();
            final UnixFSJournalEntry journalEntry = entry.getOriginal(UnixFSJournalEntry.class);
            final NodeId nodeId = journalEntry.getNodeId();
            final UnixFSTransactionProgram transactionProgram = journalEntry.getProgram();
            final UnixFSTransactionProgramInterpreter interpreter = transactionProgram.interpreter();
            final ExecutionHandler executionHandler = newExecutionHandler(nodeId, revision);
            interpreter.executeCommitPhase(executionHandler);
        }

        @Override
        public void cleanup(final TransactionJournal.Entry entry) {
            check();
            final UnixFSJournalEntry journalEntry = entry.getOriginal(UnixFSJournalEntry.class);
            final NodeId nodeId = journalEntry.getNodeId();
            final UnixFSTransactionProgram transactionProgram = journalEntry.getProgram();
            final UnixFSTransactionProgramInterpreter interpreter = transactionProgram.interpreter();
            final ExecutionHandler executionHandler = newExecutionHandler(nodeId, revision);
            interpreter.executeCleanupPhase(executionHandler);
        }

        @Override
        public void fail() {
            check();
            final UnixFSChecksumAlgorithm preferredAlgorithm = getPreferredChecksum();
            final UnixFSRevisionTableEntry tableEntry = monitor.getScope().getValue();
            tableEntry.state.set(FAILED);
            tableEntry.algorithm.set(preferredAlgorithm);
            preferredAlgorithm.compute(tableEntry);
        }

        private void check() {
            if (!open) throw new IllegalStateException();
        }

        @Override
        public Revision<?> getRevision() {
            return revision;
        }

        @Override
        public void close() {

            open = false;
            monitor.close();

            if (revisionTable.size() >= REVISION_COLLECTION_THRESHOLD) {
                getGarbageCollector().hintImmediateAsync();
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
            try (final LockedRevision lr = lockEntryWithRevision(revision)) {
                try {
                    interpreter.executeCommitPhase(executionHandler);
                } finally {
                    interpreter.executeCleanupPhase(executionHandler);
                }
            }
        }
    }

}
