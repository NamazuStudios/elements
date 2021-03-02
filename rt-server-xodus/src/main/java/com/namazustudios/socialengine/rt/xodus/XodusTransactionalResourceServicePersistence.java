package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.transact.ExclusiveReadWriteTransaction;
import com.namazustudios.socialengine.rt.transact.ReadOnlyTransaction;
import com.namazustudios.socialengine.rt.transact.ReadWriteTransaction;
import com.namazustudios.socialengine.rt.transact.TransactionalResourceServicePersistence;
import jetbrains.exodus.env.Environment;

public class XodusTransactionalResourceServicePersistence implements TransactionalResourceServicePersistence {

    private Environment environment;

    @Override
    public ReadOnlyTransaction openRO(NodeId nodeId) {
        return null;
    }

    @Override
    public ReadWriteTransaction openRW(NodeId nodeId) {
        return null;
    }

    @Override
    public ExclusiveReadWriteTransaction openExclusiveRW(NodeId nodeId) {
        return null;
    }

}
