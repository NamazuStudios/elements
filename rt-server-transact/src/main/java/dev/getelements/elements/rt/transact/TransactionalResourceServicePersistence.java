package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.id.NodeId;

import static java.lang.String.format;

/**
 * Supplies instances of {@link ReadOnlyTransaction} as well as {@link ReadWriteTransaction} for manipulating the
 * unerlying data storage.
 *
 */
public interface TransactionalResourceServicePersistence {

    /**
     * Starts building an instance of {@link ReadOnlyTransaction} against the underlying data store.
     *
     * @param nodeId the {@link NodeId} to use for the transaction context.
     *
     * @return the {@link ReadOnlyTransaction}
     */
    ReadOnlyTransaction.Builder<ReadOnlyTransaction> buildRO(NodeId nodeId);

    /**
     * Starts building an instance of {@link ReadWriteTransaction} against the underlying data store.
     *
     * @param nodeId the {@link NodeId} to use for the transaction context.
     *
     * @return the {@link ReadOnlyTransaction}
     */
    ReadOnlyTransaction.Builder<ReadWriteTransaction> buildRW(NodeId nodeId);

    /**
     * Opens a {@link ExclusiveReadWriteTransaction} which will lock the entire underlying data storage such that
     * certain operations may be performed against the whole dataset. Generally this should be avoided as it incurs a
     * steep performance penalty.
     *
     * @return the {@link ExclusiveReadWriteTransaction}
     */
    ExclusiveReadWriteTransaction openExclusiveRW();

}
