package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.util.Monitor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;

public class SkipListTransactionalResourceServiceCache implements TransactionalResourceServiceCache {

    private final Semaphore global = new Semaphore(Integer.MAX_VALUE);

    private final Set<ResourceId> locks = new ConcurrentSkipListSet<>();

    private final Map<ResourceId, TransactionalResource> cache = new ConcurrentSkipListMap<>();

    @Override
    public Mutator mutate(final ResourceId resourceId) {

        return null;
    }

    private class BasicMutator implements Mutator {

        private final Monitor global;
        private final Monitor resource;
        private final ResourceId resourceId;

        public BasicMutator(final ResourceId resourceId) {
            this.resourceId = resourceId;
        }

        @Override
        public void close() {
            resource.close();
            global.close();
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public TransactionalResource acquire() {
            return null;
        }

        @Override
        public TransactionalResource acquireInitial(Resource loaded) {
            return null;
        }

        @Override
        public <T> T release(ReleaseOperation<T> onRelease) {
            return null;
        }

        @Override
        public void purge() {

        }

        @Override
        public TransactionalResource getResource() {
            return null;
        }
    }


}
