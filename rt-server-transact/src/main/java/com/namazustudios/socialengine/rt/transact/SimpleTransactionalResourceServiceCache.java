package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.util.ConcurrentReferenceMap;
import com.namazustudios.socialengine.rt.util.FinallyAction;
import com.namazustudios.socialengine.rt.util.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class SimpleTransactionalResourceServiceCache implements TransactionalResourceServiceCache {

    private static final Logger logger = LoggerFactory.getLogger(SimpleTransactionalResourceServiceCache.class);

    private final Semaphore global = new Semaphore(Integer.MAX_VALUE);

    private final Map<ResourceId, Lock> locks = new ConcurrentReferenceMap.Builder<ResourceId, Lock>()
        .withSoftRef()
        .build();

    private final Map<ResourceId, TransactionalResource> cache = new ConcurrentSkipListMap<>();

    @Override
    public Monitor readMonitor() {
        return Monitor.enter(global);
    }

    @Override
    public long size() {
        return cache.size();
    }

    @Override
    public ExclusiveMutator exclusive() {
        final var monitor = Monitor.enter(global, Integer.MAX_VALUE);
        return new SimpleExclusiveMutator(monitor);
    }

    @Override
    public Mutator mutate(final ResourceId resourceId) {
        final var lock = locks.computeIfAbsent(resourceId, k -> new ReentrantLock());
        final var monitor = Monitor.enter(global).then(lock);
        return new SimpleMutator(monitor, resourceId);
    }

    private class SimpleMutator implements Mutator {

        private final Monitor monitor;

        private final ResourceId resourceId;

        public SimpleMutator(final Monitor monitor, final ResourceId resourceId) {
            this.monitor = monitor;
            this.resourceId = resourceId;
        }

        @Override
        public void close() {
            monitor.close();
        }

        @Override
        public boolean isPresent() {
            return cache.containsKey(resourceId);
        }

        @Override
        public TransactionalResource acquire() {

            final var tr = cache.get(resourceId);

            if (tr == null) {
                throw new IllegalStateException("Resource not acquired.");
            }

            tr.acquire();
            return tr;

        }

        @Override
        public TransactionalResource acquire(final Resource loaded) {

            final var resourceId = loaded.getId();

            if (cache.containsKey(resourceId)) {
                throw new IllegalStateException("Already acquired.");
            }

            final var tr = new TransactionalResource(SimpleTransactionalResourceServiceCache.this, loaded);
            cache.put(resourceId, tr);

            return tr;

        }

        @Override
        public void purge() {
            cache.remove(resourceId);
        }

        private TransactionalResource resource;

        @Override
        public TransactionalResource getResource() {
            return resource == null ? resource = cache.get(resourceId) : resource;
        }

    }

    private class SimpleExclusiveMutator implements ExclusiveMutator {

        private FinallyAction onClose;

        public SimpleExclusiveMutator(final Monitor monitor) {
            onClose = FinallyAction
                .begin(logger)
                .then(monitor::close);
        }

        @Override
        public Stream<Resource> clear() {
            onClose = FinallyAction.with(cache::clear).then(onClose);
            return cache.values()
                .stream()
                .map(TransactionalResource::getDelegate);
        }

        @Override
        public void close() {
            onClose.close();
        }

    }

}
