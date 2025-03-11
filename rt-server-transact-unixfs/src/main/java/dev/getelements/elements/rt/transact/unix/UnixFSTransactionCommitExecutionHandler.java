package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnixFSTransactionCommitExecutionHandler implements UnixFSTransactionProgramInterpreter.ExecutionHandler {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTransactionCommitExecutionHandler.class);

    private final DataStore dataStore;

    public UnixFSTransactionCommitExecutionHandler( final DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void applyContentsChange(
            final UnixFSTransactionProgram program,
            final UnixFSTransactionCommand command,
            final ResourceId resourceId,
            final String transactionId) {
        getDataStore().getResourceIndex().applyContentsChange(resourceId, transactionId);
    }

    @Override
    public void applyReversePathsChange(
            final UnixFSTransactionProgram program,
            final UnixFSTransactionCommand command,
            final ResourceId resourceId,
            final String transactionId) {
        getDataStore().getResourceIndex().applyReversePathsChange(resourceId, transactionId);
    }

    @Override
    public void applyPathChange(
            final UnixFSTransactionProgram program,
            final UnixFSTransactionCommand command,
            final Path rtPath,
            final String transactionId) {
        getDataStore().getPathIndex().applyChange(rtPath, transactionId);
    }

    @Override
    public void applyTaskChanges(
            final UnixFSTransactionProgram program,
            final UnixFSTransactionCommand command,
            final ResourceId resourceId,
            final String transactionId) {
        getDataStore().getTaskIndex().applyChange(resourceId, transactionId);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

}
