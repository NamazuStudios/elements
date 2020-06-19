package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.id.NodeId;

/**
 * Supplies instances of {@link ReadOnlyTransaction} as well as {@link ReadWriteTransaction} for manipulating the
 * unerlying data storage.
 *
 */
public interface TransactionalResourceServicePersistence extends AutoCloseable {

    /**
     * Opens an instance of {@link ReadOnlyTransaction} with the underlying data store.
     *
     * @param nodeId the {@link NodeId} to use for the transaction context.
     *
     * @return the {@link ReadOnlyTransaction}
     */
    ReadOnlyTransaction openRO(NodeId nodeId);

    /**
     * Opens an instance of {@link ReadWriteTransaction} with the underlying data store.
     *
     * @param nodeId the {@link NodeId} to use for the transaction context.
     *
     * @return the {@link ReadWriteTransaction}
     */
    ReadWriteTransaction openRW(NodeId nodeId);

    /**
     * Opens a {@link ExclusiveReadWriteTransaction} which will lock the entire underlying data storage such that
     * certain operations may be performed against the whole dataset. Generally this should be avoided as it incurs a
     * steep performance penalty.
     *
     * @param nodeId the {@link NodeId} to use for the transaction context.
     *
     * @return the {@link ExclusiveReadWriteTransaction}
     */
    ExclusiveReadWriteTransaction openExclusiveRW(NodeId nodeId);

    /**
     * Closes this {@link TransactionalResourceServicePersistence} instance and releases any underlying connections to
     * the data storage. Outstanding transactions may be forcibly closed if this is called.
     */
    @Override
    void close();

}
