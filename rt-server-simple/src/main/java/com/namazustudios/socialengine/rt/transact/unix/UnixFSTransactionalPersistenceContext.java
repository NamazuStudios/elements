package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.transact.TransactionalPersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class UnixFSTransactionalPersistenceContext implements TransactionalPersistenceContext {

    private final Logger logger = LoggerFactory.getLogger(UnixFSTransactionalPersistenceContext.class);

    private final UnixFSUtils unixFSUtils;

    private final UnixFSTransactionJournal transactionJournal;

    private final UnixFSRevisionTable revisionTable;

    private final UnixFSGarbageCollector garbageCollector;

    private final UnixFSRevisionPool revisionPool;

    @Inject
    public UnixFSTransactionalPersistenceContext(
            final UnixFSUtils unixFSUtils,
            final UnixFSTransactionJournal transactionJournal,
            final UnixFSRevisionTable revisionTable,
            final UnixFSGarbageCollector garbageCollector,
            final UnixFSRevisionPool revisionPool) {
        this.unixFSUtils = unixFSUtils;
        this.transactionJournal = transactionJournal;
        this.revisionTable = revisionTable;
        this.garbageCollector = garbageCollector;
        this.revisionPool = revisionPool;
    }

    @Override
    public void start() {

        getUnixFSUtils().lockStorageRoot();

        try {
            getRevisionPool().start();
            getRevisionTable().start();
            getGarbageCollector().start();
            getTransactionJournal().start();
        } catch (IllegalStateException ex) {
            logger.error("Inconsistent state.", ex);
        } catch (Exception ex) {
            stop();
            logger.error("Failed to start.", ex);
        }

    }

    @Override
    public void stop() {
        try {
            safeStop(getTransactionJournal()::stop);
            safeStop(getGarbageCollector()::stop);
            safeStop(getRevisionTable()::stop);
            safeStop(getRevisionPool()::stop);
        } finally {
            getUnixFSUtils().unlockStorageRoot();
        }
    }

    private void safeStop(final Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            logger.error("Caught exception stopping service.", ex);
        }
    }

    public UnixFSRevisionPool getRevisionPool() {
        return revisionPool;
    }

    public UnixFSUtils getUnixFSUtils() {
        return unixFSUtils;
    }

    public UnixFSTransactionJournal getTransactionJournal() {
        return transactionJournal;
    }

    public UnixFSRevisionTable getRevisionTable() {
        return revisionTable;
    }

    public UnixFSGarbageCollector getGarbageCollector() {
        return garbageCollector;
    }

}
