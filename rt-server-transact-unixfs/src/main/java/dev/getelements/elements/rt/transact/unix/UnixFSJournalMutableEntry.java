package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.DataStore;
import dev.getelements.elements.rt.transact.TransactionJournal;
import dev.getelements.elements.sdk.util.FinallyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionProgramExecutionPhase.CLEANUP;
import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionProgramExecutionPhase.COMMIT;

class UnixFSJournalMutableEntry implements TransactionJournal.MutableEntry {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSJournalMutableEntry.class);

    // Initialized with Object
    private boolean open = true;

    private boolean rollback = false;

    private boolean committed = false;

    // Assigned in Constructor

    private final String transactionId;

    private final UnixFSTransactionProgramBuilder programBuilder;

    private final DataStore dataStore;

    private final Consumer<UnixFSJournalMutableEntry> onWrite;

    private final FinallyAction onClose;

    public UnixFSJournalMutableEntry(final String transactionId,
                                     final DataStore dataStore,
                                     final UnixFSTransactionProgramBuilder programBuilder,
                                     final Consumer<UnixFSJournalMutableEntry> onWrite,
                                     final Consumer<UnixFSJournalMutableEntry> onClose) {
        this.transactionId = transactionId;
        this.programBuilder = programBuilder;
        this.dataStore = dataStore;
        this.onWrite = onWrite;
        this.onClose = FinallyAction.begin(logger)
                .then(() -> {
                    try {
                        if (!committed && !rollback) {
                            rollback();
                        }
                    } finally {
                        open = false;
                    }
                })
                .then(() -> onClose.accept(this));
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public void applyChangeToResourceReversePaths(final Path path) {
        getProgramBuilder().applyReversePathChangeToResource(COMMIT, path);
        getProgramBuilder().cleanupResource(CLEANUP, path);
    }

    @Override
    public void applyChangeToResourceReversePaths(final ResourceId resourceId) {
        getProgramBuilder().applyReversePathChangeToResource(COMMIT, resourceId);
        getProgramBuilder().cleanupResource(CLEANUP, resourceId);
    }

    @Override
    public void applyChangeToResourceContents(final ResourceId resourceId) {
        getProgramBuilder().applyChangeToResourceContents(COMMIT, resourceId);
    }

    @Override
    public void applyChangeToTasks(final ResourceId resourceId) {
        getProgramBuilder().applyChangeToTasks(COMMIT, resourceId);
        getProgramBuilder().cleanupTasksForResource(CLEANUP, resourceId);
    }

    @Override
    public void commit() {

        check();
        committed = true;

        final var program = programBuilder
                .withTransactionId(getTransactionId())
                .compile(COMMIT, CLEANUP)
                .commit()
                .interpreter();

        onWrite.accept(this);

        final var handler = new UnixFSTransactionCommitExecutionHandler(dataStore);
        program.executeCommitPhase(handler).executeCleanupPhase(handler);

    }

    @Override
    public void rollback() {

        check();

        rollback = true;

        final var program = programBuilder
                .withTransactionId(getTransactionId())
                .compile(CLEANUP)
                .commit()
                .interpreter();

        onWrite.accept(this);

        final var handler = new UnixFSTransactionRollbackExecutionHandler(dataStore);
        program.executeCleanupPhase(handler);

    }

    @Override
    public void close() {
        onClose.close();
    }

    private void check() {
        if (!open) {
            throw new IllegalStateException("Transaction is closed.");
        } else if (committed) {
            throw new IllegalStateException("Transaction is committed.");
        } else if (rollback) {
            throw new IllegalStateException("Transaction is rolled-back");
        }
    }

    public UnixFSTransactionProgramBuilder getProgramBuilder() {
        return programBuilder;
    }

}
