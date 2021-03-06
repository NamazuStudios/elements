package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Persistence;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;

import static java.lang.String.format;

/**
 * Supplies instances of {@link ReadOnlyTransaction} as well as {@link ReadWriteTransaction} for manipulating the
 * unerlying data storage.
 *
 */
public interface TransactionalResourceServicePersistence {

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

}
