package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.cluster.id.ResourceId;

public interface TransactionJournal {

    /**
     * Gets a new entry for writing.
     *
     * @return a new {@link MutableEntry}
     * @param nodeId the {@link NodeId} to use
     */
    MutableEntry newMutableEntry(NodeId nodeId);

    /**
     * Represents a mutable journal entry.  This allows for writes to
     */
    interface MutableEntry extends AutoCloseable {

        /**
         * Gets the unique ID of the transaction.
         *
         * @return the transaction ID
         */
        String getTransactionId();

        /**
         * Commits the pending changes to the {@link TransactionJournal}.  Once this is done the next subsequent
         * operation must be to close the {@link MutableEntry} as it will accept no further mutations. If left
         * uncommitted at close time, the changes to the journal will roll back and result in an error in the logs.
         */
        void commit();

        /**
         * Rolls back the pending changes to the {@link TransactionJournal}.  Once this is done the next subsequent
         * operation must be to close the {@link MutableEntry} as it will accept no further mutations.
         */
        void rollback();

        /**
         * Logs the supplied {@link Path} as having been flushed.
         * @param path
         */
        void applyChangeToResourceReversePaths(Path path);

        /**
         * Logs the supplied {@link ResourceId} as having been flushed.
         * @param resourceId
         */
        void applyChangeToResourceReversePaths(ResourceId resourceId);

        /**
         * Logs the supplied {@link ResourceId} as its contents having been flushed.
         *
         * @param resourceId
         */
        void applyChangeToResourceContents(ResourceId resourceId);

        /**
         * Applies the changes to the tasks for the supplied {@link ResourceId}.
         * @param resourceId
         */
        void applyChangeToTasks(ResourceId resourceId);

        /**
         * Closes this entry.  Releasing any resources to the underlying {@link TransactionJournal}.  Once this is
         * called, all other methods must throw an instance of {@link IllegalStateException} and commit no changes to
         * the underlying {@link TransactionJournal}.
         */
        @Override
        void close();

    }

}
