package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.transact.TransactionalPersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.io.IOException;

import static java.nio.file.Files.createDirectories;

public class UnixFSTransactionalPersistenceContext implements TransactionalPersistenceContext {

    private final Logger logger = LoggerFactory.getLogger(UnixFSTransactionalPersistenceContext.class);

    private final UnixFSUtils unixFSUtils;

    private final UnixFSTransactionJournal transactionJournal;

    private final UnixFSRevisionTable revisionTable;

    private final UnixFSGarbageCollector garbageCollector;

    private final UnixFSRevisionPool revisionPool;

    private final UnixFSRevisionDataStore revisionDataStore;

    @Inject
    public UnixFSTransactionalPersistenceContext(
            final UnixFSUtils unixFSUtils,
            final UnixFSTransactionJournal transactionJournal,
            final UnixFSRevisionTable revisionTable,
            final UnixFSGarbageCollector garbageCollector,
            final UnixFSRevisionPool revisionPool,
            final UnixFSRevisionDataStore unixFSRevisionDataStore) {
        this.unixFSUtils = unixFSUtils;
        this.transactionJournal = transactionJournal;
        this.revisionTable = revisionTable;
        this.garbageCollector = garbageCollector;
        this.revisionPool = revisionPool;
        this.revisionDataStore = unixFSRevisionDataStore;
    }

    @Override
    public void start() {

        getUnixFSUtils().initialize();
        getUnixFSUtils().lockStorageRoot();

        try {
            getRevisionPool().start();
            getRevisionTable().start();
            getTransactionJournal().start();
            getGarbageCollector().start();
            getRevisionDataStore().start();
        } catch (IllegalStateException ex) {
            logger.error("Inconsistent state.", ex);
            throw ex;
        } catch (Exception ex) {
            stop();
            logger.error("Failed to start.", ex);
            throw new InternalException(ex);
        }

    }

    @Override
    public void stop() {
        try {
            safeStop(getRevisionDataStore()::stop);
            safeStop(getGarbageCollector()::stop);
            safeStop(getTransactionJournal()::stop);
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

    public UnixFSRevisionDataStore getRevisionDataStore() {
        return revisionDataStore;
    }

}
