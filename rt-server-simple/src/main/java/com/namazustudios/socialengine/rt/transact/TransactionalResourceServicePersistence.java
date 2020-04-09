package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.util.Spliterator;

public interface TransactionalResourceServicePersistence extends AutoCloseable {

    ReadOnlyTransaction openRO();

    ReadWriteTransaction openRW();

    ExclusiveReadWriteTransaction openExclusiveRW();

    @Override
    void close();

}
