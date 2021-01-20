package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.transact.TransactionalPersistenceContext;
import com.namazustudios.socialengine.rt.util.ShutdownHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class UnixFSTransactionalPersistenceContext implements TransactionalPersistenceContext {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTransactionalPersistenceContext.class);

    private static final ShutdownHooks shutdownHooks = new ShutdownHooks(UnixFSTransactionalPersistenceContext.class);

    private UnixFSUtils unixFSUtils;

    private UnixFSTransactionJournal transactionJournal;

    private UnixFSRevisionTable revisionTable;

    private UnixFSGarbageCollector garbageCollector;

    private UnixFSRevisionPool revisionPool;

    private UnixFSRevisionDataStore revisionDataStore;

    public UnixFSTransactionalPersistenceContext() {
        shutdownHooks.add(this, () -> {
            try {
                stop();
            } catch (IllegalStateException ex) {
                // This is expected because under normal conditions this should be
                // shut down properly. This is just a failsafe to aid in debugging
                // and protect as much data integrity as absolutely possible.
                logger.trace("Not running. Ignoring exception.", ex);
            }
        });
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
            doStop(false);
            logger.error("Failed to start.", ex);
            throw new InternalException(ex);
        }

    }

    @Override
    public void stop() {
        doStop(true);
    }

    private void doStop(final boolean clean) {
        try {
            tryRun(getGarbageCollector()::stop, clean);
            tryRun(getRevisionDataStore()::stop, clean);
            tryRun(getTransactionJournal()::stop, clean);
            tryRun(getRevisionTable()::stop, clean);
            tryRun(getRevisionPool()::stop, clean);
        } finally {
            getUnixFSUtils().unlockStorageRoot();
        }
    }

    private void tryRun(final Runnable action, final boolean clean) {
        try {
            action.run();
        } catch (IllegalStateException ex) {
            if (clean) {
                logger.error("Caught exception stopping service.", ex);
            } else {
                // With an unclean startup, it's expected that we may get some IllegalStateExceptions due to the fact
                // that the services have not started up completely. It's simpler to log this as a trace message just
                logger.trace("Caught exception stopping service.", ex);
            }
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
