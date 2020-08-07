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

    private UnixFSUtils unixFSUtils;

    private UnixFSTransactionJournal transactionJournal;

    private UnixFSRevisionTable revisionTable;

    private UnixFSGarbageCollector garbageCollector;

    private UnixFSRevisionPool revisionPool;

    private UnixFSRevisionDataStore revisionDataStore;

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

    public UnixFSUtils getUnixFSUtils() {
        return unixFSUtils;
    }

    @Inject
    public void setUnixFSUtils(UnixFSUtils unixFSUtils) {
        this.unixFSUtils = unixFSUtils;
    }

    public UnixFSTransactionJournal getTransactionJournal() {
        return transactionJournal;
    }

    @Inject
    public void setTransactionJournal(UnixFSTransactionJournal transactionJournal) {
        this.transactionJournal = transactionJournal;
    }

    public UnixFSRevisionTable getRevisionTable() {
        return revisionTable;
    }

    @Inject
    public void setRevisionTable(UnixFSRevisionTable revisionTable) {
        this.revisionTable = revisionTable;
    }

    public UnixFSGarbageCollector getGarbageCollector() {
        return garbageCollector;
    }

    @Inject
    public void setGarbageCollector(UnixFSGarbageCollector garbageCollector) {
        this.garbageCollector = garbageCollector;
    }

    public UnixFSRevisionPool getRevisionPool() {
        return revisionPool;
    }

    @Inject
    public void setRevisionPool(UnixFSRevisionPool revisionPool) {
        this.revisionPool = revisionPool;
    }

    public UnixFSRevisionDataStore getRevisionDataStore() {
        return revisionDataStore;
    }

    @Inject
    public void setRevisionDataStore(UnixFSRevisionDataStore revisionDataStore) {
        this.revisionDataStore = revisionDataStore;
    }

}
