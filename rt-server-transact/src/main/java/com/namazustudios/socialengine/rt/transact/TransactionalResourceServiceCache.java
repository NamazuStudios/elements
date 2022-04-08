package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.util.Monitor;

import java.util.stream.Stream;

public interface TransactionalResourceServiceCache {

    long size();

    Monitor readMonitor();

    Mutator mutate(ResourceId resourceId);

    ExclusiveMutator exclusive();

    interface Mutator extends AutoCloseable {

        boolean isPresent();

        void purge();

        TransactionalResource acquire();

        TransactionalResource acquire(Resource loaded);

        TransactionalResource getResource();

        @Override
        void close();

    }

    interface ExclusiveMutator extends AutoCloseable {

        Stream<Resource> clear();

        @Override
        void close();

    }

}
