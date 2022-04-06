package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.util.stream.Stream;

public interface TransactionalResourceServiceCache {

    long size();

    Mutator mutate(ResourceId resourceId);

    ExclusiveMutator exclusive();

    interface Mutator extends AutoCloseable {

        @Override
        void close();

        boolean isPresent();

        TransactionalResource acquire();

        TransactionalResource acquireInitial(Resource loaded);

        <T> T release(ReleaseOperation<T> onRelease);

        void purge();

        TransactionalResource getResource();

    }

    interface ExclusiveMutator extends AutoCloseable {

        Stream<Resource> clear();

        @Override
        void close();

    }

    @FunctionalInterface
    interface ReleaseOperation<T> {

        T perform(int acquires);

    }
}
