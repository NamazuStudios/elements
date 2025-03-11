package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnixFSTransactionRollbackExecutionHandler implements UnixFSTransactionProgramInterpreter.ExecutionHandler {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTransactionRollbackExecutionHandler.class);

    private final DataStore dataStore;

    public UnixFSTransactionRollbackExecutionHandler(final DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void cleanupPath(
            final UnixFSTransactionProgram program,
            final UnixFSTransactionCommand command,
            final Path rtPath,
            final String transactionId) {
        dataStore.getPathIndex().cleanup(rtPath, transactionId);
    }

    @Override
    public void cleanupResourceId(
            final UnixFSTransactionProgram program,
            final UnixFSTransactionCommand command,
            final ResourceId resourceId,
            final String transactionId) {
        dataStore.getResourceIndex().cleanup(resourceId, transactionId);
    }

    @Override
    public void cleanupTasks(
            final UnixFSTransactionProgram program,
            final UnixFSTransactionCommand command,
            final ResourceId resourceId,
            final String transactionId) {
        dataStore.getTaskIndex().cleanup(resourceId, transactionId);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

}
