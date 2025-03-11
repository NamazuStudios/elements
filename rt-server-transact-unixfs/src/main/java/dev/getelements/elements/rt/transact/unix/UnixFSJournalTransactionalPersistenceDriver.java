package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.transact.JournalTransactionalPersistenceDriver;
import dev.getelements.elements.sdk.util.ShutdownHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

public class UnixFSJournalTransactionalPersistenceDriver implements JournalTransactionalPersistenceDriver {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSJournalTransactionalPersistenceDriver.class);

    private static final ShutdownHooks shutdownHooks = new ShutdownHooks(UnixFSJournalTransactionalPersistenceDriver.class);

    private UnixFSUtils unixFSUtils;

    private UnixFSTransactionJournal transactionJournal;

    public UnixFSJournalTransactionalPersistenceDriver() {
        shutdownHooks.add(this, () -> doStop(false));
    }

    @Override
    public void start() {
        getUnixFSUtils().initialize();
        getUnixFSUtils().lockStorageRoot();
        getTransactionJournal().start();
    }

    @Override
    public void stop() {
        doStop(true);
    }

    private void doStop(final boolean clean) {
        tryRun(getTransactionJournal()::stop, clean);
        tryRun(getUnixFSUtils()::unlockStorageRoot, clean);
    }

    private void tryRun(final Runnable action, final boolean clean) {
        try {
            action.run();
        } catch (IllegalStateException ex) {
            if (clean) {
                // If it is a clean shutdown then errors.
                logger.error("Caught exception stopping service.", ex);
            } else {
                // With an unclean shutdown, it's expected that we may get some IllegalStateExceptions due to the fact
                // that the services have not started up completely. It's simpler to log this as a trace message and is
                // otherwise expected to get this exception.
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

}
