package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.exception.ContentionException;
import com.namazustudios.socialengine.rt.exception.DuplicateException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;
import static java.util.Spliterator.*;
import static java.util.stream.Collectors.toList;

public class TransactionalResourceService implements ResourceService {

    private static final int RETRY_COUNT = 250;

    private static final int WAIT_INTERVAL = 5;

    private static final Logger logger = LoggerFactory.getLogger(TransactionalResourceService.class);

    private NodeId nodeId;

    private ResourceLoader resourceLoader;

    private TransactionalResourceServicePersistence persistence;

    private final AtomicReference<Context> context = new AtomicReference<>();

    @Override
    public void start() {

        final Context context = new Context();

        if (this.context.compareAndSet(null, context)) {
            logger.info("Started.");
        } else {
            throw new IllegalStateException("Already running.");
        }

    }

    @Override
    public void stop() {
        final Context context = this.context.getAndSet(null);
        if (context == null) throw new IllegalStateException("Not currently running.");
    }

    @Override
    public boolean exists(final ResourceId resourceId) {
        return computeRO(txn -> txn.exists(resourceId));
    }

    @Override
    public Resource getAndAcquireResourceWithId(final ResourceId resourceId) {
        return computeRO((acm, txn) -> {
            if (!txn.exists(resourceId)) throw new ResourceNotFoundException();
            return acm.acquire(resourceId);
        });
    }

    @Override
    public Resource getAndAcquireResourceAtPath(final Path path) {
        return computeRO((acm, txn) -> {
            final Path normalized = normalize(path);
            final ResourceId resourceId = txn.getResourceId(normalized);
            return acm.acquire(resourceId);
        });
    }

    @Override
    public Resource addAndAcquireResource(final Path path, final Resource resource) {
        return computeRW((acm, txn) -> {
            final Path normalized = normalize(path);
            txn.linkNewResource(normalized, resource.getId());
            return acm.acquire(resource);
        });
    }

    @Override
    public void addAndReleaseResource(final Path path, final Resource resource) {
        executeRW(txn -> {

            final Path normalized = normalize(path);

            try (final Resource r = resource;
                 final WritableByteChannel wbc = txn.saveNewResource(normalized, r.getId())) {
                r.serialize(wbc);
            } catch (IOException ex) {
                throw new InternalException(ex);
            }

        });
    }

    @Override
    public boolean tryRelease(final Resource resource) {

        final TransactionalResource transactionalResource;

        try {
            transactionalResource = (TransactionalResource) resource;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("Resource not owned by this ResourceService", ex);
        }

        return computeRW((acm, txn) -> {
            if (txn.exists(resource.getId())) {
                return acm.tryRelease(transactionalResource);
            } else {
                return false;
            }
        });

    }

    @Override
    public Spliterator<Listing> list(final Path path) {
        return computeRO(txn -> {
            final Path normalized = normalize(path);
            final List<Listing> listings = txn.list(normalized).collect(toList());
            return Spliterators.spliterator(listings, SUBSIZED | CONCURRENT | NONNULL);
        });
    }

    @Override
    public void link(final ResourceId sourceResourceId, final Path destination) {
        executeRW(txn -> {
            final Path normalized = normalize(destination);
            txn.linkExistingResource(sourceResourceId, normalized);
        });
    }

    @Override
    public Unlink unlinkPath(final Path path, final Consumer<Resource> removed) {
        return computeRW((acm, txn) -> {

            final Path normalized = normalize(path);
            final ResourceId resourceId = txn.getResourceId(normalized);

            try (final ReadableByteChannel rbc = txn.loadResourceContents(resourceId)) {

                final Unlink unlink = txn.unlinkPath(normalized);

                if (unlink.isRemoved()) {
                    final Resource resource = getResourceLoader().load(rbc);
                    removed.accept(resource);
                }

                return unlink;

            } catch (NullResourceException ex) {
                final Unlink unlink = txn.unlinkPath(normalized);
                acm.evict(resourceId, removed);
                return unlink;
            } catch (IOException ex) {
                throw new InternalException(ex);
            }

        });
    }

    @Override
    public List<Unlink> unlinkMultiple(final Path path, final int max, final Consumer<Resource> removed) {
        return computeRW(txn -> {
            final Path normalized = normalize(path);
            return txn.unlinkMultiple(normalized, max);
        });
    }

    @Override
    public Resource removeResource(final ResourceId resourceId) {
        return computeRW((acm, txn) -> {
            try (final ReadableByteChannel rbc = txn.loadResourceContents(resourceId)){
                txn.removeResource(resourceId);
                return getResourceLoader().load(rbc);
            } catch (NullResourceException ex) {
                txn.removeResource(resourceId);
                return acm.evict(resourceId);
            } catch (IOException e) {
                throw new InternalException(e);
            }
        });
    }

    @Override
    public List<ResourceId> removeResources(final Path path, final int max, final Consumer<Resource> removed) {
        return computeRW((acm, txn) -> {
            final Path normalized = normalize(path);
            final List<ResourceId> resourceIds = txn.removeResources(normalized, max);
            resourceIds.forEach(resourceId -> acm.evict(resourceId, removed));
            return resourceIds;
        });
    }

    private Path normalize(final Path path) {
        return path.hasContext() ? path : path.toPathWithContext(getNodeId().asString());
    }

    @Override
    public Stream<Resource> removeAllResources() {

        final Context context = getContext();

        try (final ExclusiveReadWriteTransaction txn = getPersistence().openExclusiveRW(getNodeId())) {
            final Context old = this.context.getAndSet(new Context());
            txn.removeAllResources();
            return old.acquires.values().stream().map(tr -> tr.getDelegate());
        }

    }

    @Override
    public long getInMemoryResourceCount() {
        final Context context = getContext();
        return context.acquires.size();
    }

    public void persist(final ResourceId resourceId) {
        executeRW((acm, txn) -> {
            try (final WritableByteChannel wbc = txn.updateResource(resourceId)) {
                acm.updateResource(resourceId, wbc);
            } catch (IOException ex) {
                throw new InternalException(ex);
            }
        });
    }

    public NodeId getNodeId() {
        return nodeId;
    }

    @Inject
    public void setNodeId(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Inject
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public TransactionalResourceServicePersistence getPersistence() {
        return persistence;
    }

    @Inject
    public void setPersistence(final TransactionalResourceServicePersistence persistence) {
        this.persistence = persistence;
    }

    private Context getContext() {
        final Context context = this.context.get();
        if (context == null) throw new IllegalStateException("Not started.");
        return context;
    }

    private <T> T computeRO(final Function<ReadOnlyTransaction, T> operation) {
        try (final ReadOnlyTransaction txn = getPersistence().openRO(getNodeId())) {
            return operation.apply(txn);
        }
    }

    private <T> T computeRO(final BiFunction<AcquiresCacheMutator, ReadOnlyTransaction, T> operation) {

        final Context context = getContext();

        try (final ReadOnlyTransaction txn = getPersistence().openRO(getNodeId());
             final AcquiresCacheMutator acm = new AcquiresCacheMutator(context, txn)) {
            final T value = operation.apply(acm, txn);
            return value;
        }

    }

    private void executeRW(final TransactionOperationV operation) {

        final Context context = getContext();

        for (int i = 0; i < RETRY_COUNT; ++i) {
            try (final ReadWriteTransaction txn = getPersistence().openRW(getNodeId());
                 final AcquiresCacheMutator acm = new AcquiresCacheMutator(context, txn)) {
                operation.apply(acm, txn);
                txn.commit();
                return;
            } catch (TransactionConflictException ex) {
                randomWait(i);
                continue;
            }
        }

        throw new ContentionException();

    }

    private void executeRW(final UncachedTransactionOperationV operation) {

        for (int i = 0; i < RETRY_COUNT; ++i) {
            try (final ReadWriteTransaction txn = getPersistence().openRW(getNodeId())) {
                operation.apply(txn);
                txn.commit();
                return;
            } catch (TransactionConflictException ex) {
                randomWait(i);
                continue;
            }
        }

        throw new ContentionException();

    }

    private <T> T computeRW(final UncachedTransactionOperation<T> operation) {

        final Context context = getContext();

        for (int i = 0; i < RETRY_COUNT; ++i) {
            try (final ReadWriteTransaction txn = getPersistence().openRW(getNodeId())) {
                final T value = operation.apply(txn);
                txn.commit();
                return value;
            } catch (TransactionConflictException ex) {
                randomWait(i);
                continue;
            }
        }

        throw new ContentionException();

    }

    private <T> T computeRW(final TransactionOperation<T> operation) {

        final Context context = getContext();

        for (int i = 0; i < RETRY_COUNT; ++i) {
        try (final ReadWriteTransaction txn = getPersistence().openRW(getNodeId());
                 final AcquiresCacheMutator acm = new AcquiresCacheMutator(context, txn)) {
                final T value = operation.apply(acm, txn);
                txn.commit();
                return value;
            } catch (TransactionConflictException ex) {
                randomWait(i);
                continue;
            }
        }

        throw new ContentionException();

    }

    private void randomWait(final int retry) {

        final Random random = ThreadLocalRandom.current();
        final int time = random.nextInt((retry + 1) * WAIT_INTERVAL);

        try {
            sleep(time);
        } catch (InterruptedException ex) {
            throw new InternalException(ex);
        }

    }

    private class Context {

        private final ConcurrentMap<ResourceId, TransactionalResource> acquires = new ConcurrentHashMap<>();

    }

    /**
     * A light-weight in-memory utility used to maniuplate the acquires cache.  This is somewhat like a transaction
     * but lacks the ability to roll back.  It implements it's logic using a optimistic appraoch combined with
     * the concept of "roll-forward" mechanics.
     *
     * This may implicity acquire and release a single resource many times but makes efforts to ensure that the net
     * result of all operations result in precisely one acquire or one release.
     */
    private class AcquiresCacheMutator implements AutoCloseable {

        private final Context context;

        private final ReadOnlyTransaction txn;

        private final List<TransactionalResource> toClose = new ArrayList<>();

        private final List<TransactionalResource> toAcquire = new ArrayList<>();

        private final List<TransactionalResource> toRelease = new ArrayList<>();

        public AcquiresCacheMutator(final Context context,
                                    final ReadOnlyTransaction txn) {
            this.txn = txn;
            this.context = context;
        }

        @Override
        public void close() {

            toAcquire.forEach(r -> {
                // This should never fail because we conservatively acquire each resource as we encounter it even
                // among all the contention (which should be rare).
                if (!r.acquire()) logger.error("Failed to acquire {} in post-acquire loop.");
            });

            toRelease.forEach(r -> {
                // This should also never fail because of the same reason above.  Each of the acquires happens before
                // this, so even any overlaps should have been acquired.  Any of these will be purged after the
                // reference count hits zero.
                if (!r.release()) logger.error("Failed to release {} in post-acquire loop.");
            });

            toClose.forEach(r -> {
                try {
                    // We should only allow nascent-state resources to force close. acquire/release should handle
                    // all other cases as the resoures specified here were extraneously created and just need to be
                    // destroyed.
                    if (!r.isNascent()) logger.error("Forcibly closing non-nascent Resource {}", r);
                    r.close();
                } catch (Exception ex) {
                    logger.error("Caught exception closing resource {}", r, ex);
                }
            });

        }

        private TransactionalResource acquire(final Resource resource) {

            final ResourceId resourceId = resource.getId();

            final TransactionalResource transactionalResource;
            transactionalResource = new TransactionalResource(txn.getReadRevision(), resource, this::purge);
            transactionalResource.acquire();

            final TransactionalResource result = context.acquires.putIfAbsent(resourceId, transactionalResource);
            if (result != null) throw new DuplicateException("Resource already acquired.");

            return transactionalResource;

        }

        private TransactionalResource acquire(final ResourceId resourceId) {

            final TransactionalResource result = context.acquires.compute(resourceId, (k, existing) -> {

                if (existing == null || !existing.acquire()) {
                    // Either this Resource does not exist in the cache, or somebody else recently released it, so
                    // the solution is to load it in either case.
                    return loadTransactionalResource(resourceId);
                }

                // Since we have conservatively acquired the above resource, we must ensure that it will be
                // released when the operation completes.
                releaseOnClose(existing);

// TODO We need to move the updating code somewhere else
//
//                // Revisions will only ever move forward.  Therefore, we can skip an allocation completely if we check
//                // this first and just return the existing value.  We can safely assume that at some point some other
//                // thread will be in the process of applying a later revision.  In any case, the version we currently
//                // have will soon be replaced and it's not worth it.  However we have to check again to ensure we don't
//                // leak memory.
//                if (existing.getRevision().isBefore(txn.getReadRevision()));
//
//                // Take the penalty allocating a large resource.  This may still fail, but the above check should avoid
//                // any several iterations.
//                final Resource update = loadResource(resourceId);
//
//                 try (final Resource stale = existing.update(update, txn.getReadRevision())) {
//                    // Even though we may make several allocations, it's okay.  We are guaranteed to make and destroy
//                    // one resource per iteration.
//                    logger.debug("Updated resource {} -> {}", update, stale);
//                } catch (Exception ex) {
//                    logger.error("Caught exception closing out stale resource.", ex);
//                }

                return existing;

            });

            // We must make sure that it won't be closed with this cache manipulation.  This is side-effect free if we
            // didn't ever allocate a new resource in the first place.

            keep(result);

            // And we also must make sure that the resource will definitely be acquired.
            acquireOnClose(result);

            return result;

        }

        public boolean tryRelease(final TransactionalResource resource) {

            // This isn't a valid use-case for this method.  Any resource that is nascent state, we really can't make
            // sense of this so we throw an exception to avoid corrupting the cache.  This would only happen if
            // the coe to this class was modified.
            if (resource.isNascent()) throw new IllegalArgumentException("Nascent-state Resource supplied to tryRelease");

            final ResourceId resourceId = resource.getId();
            final TransactionalResource result = context.acquires.get(resourceId);

            // If the object in the map does not equal the supplied resource, then we simply fail the operation.  This
            // would only happen if the caller didn't have balanced code as we guarantee a memory-resident Resource to
            // be valid as long as it has a positive reference count.
            if (result != resource) return false;

            // Finally return true if the operation was successful.  IF this happens before an acquire, then the
            // concurrently-happening will see a dead resource and reload, or it will see an active resource and
            // acquire meaning that this release will not happen immediately.
            return resource.release();

        }

        private Resource loadResource(final ResourceId resourceId) {
            try (final ReadableByteChannel rbc = txn.loadResourceContents(resourceId)) {
                return getResourceLoader().load(rbc, false);
            } catch (IOException e) {
                throw new InternalException(e);
            }
        }

        private TransactionalResource loadTransactionalResource(final ResourceId resourceId) {
            try (final ReadableByteChannel rbc = txn.loadResourceContents(resourceId)) {

                final Resource resource = getResourceLoader().load(rbc, false);

                final TransactionalResource transactionalResource;
                transactionalResource = new TransactionalResource(txn.getReadRevision(), resource, this::purge);

                toClose.add(transactionalResource);
                return transactionalResource;

            } catch (IOException e) {
                throw new InternalException(e);
            }
        }

        private void purge(final TransactionalResource transactionalResource) {

            if (context.acquires.remove(transactionalResource.getId()) == null) {
                logger.error("Unable to evict {} from resource cache.", transactionalResource);
            }

            try {
                transactionalResource.close();
            } catch (Exception ex) {
                logger.error("Error closing resource {}", transactionalResource, ex);
            }

        }

        private void keep(final TransactionalResource transactionalResource) {
            toClose.removeIf(r -> r == transactionalResource);
        }

        private void acquireOnClose(final TransactionalResource transactionalResource) {
            toAcquire.add(transactionalResource);
        }

        private void releaseOnClose(final TransactionalResource transactionalResource) {
            toRelease.add(transactionalResource);
        }

        public Resource evict(final ResourceId resourceId) {
            final TransactionalResource tr = context.acquires.remove(resourceId);
            if (tr == null) throw new InternalException("Should have a Resource present in cache.");
            return tr.getDelegate();
        }

        public void evict(final ResourceId resourceId, final Consumer<Resource> removed) {
            final TransactionalResource tr = context.acquires.remove(resourceId);
            if (tr != null) removed.accept(tr.getDelegate());
        }

        public void updateResource(final ResourceId resourceId, final WritableByteChannel wbc) throws IOException {
            final TransactionalResource tr = context.acquires.get(resourceId);
            if (tr == null) throw new ResourceNotFoundException();
            tr.serialize(wbc);
        }

    }

    @FunctionalInterface
    private interface TransactionOperationV {

        void apply(AcquiresCacheMutator acm, ReadWriteTransaction txn) throws TransactionConflictException;

    }

    @FunctionalInterface
    private interface UncachedTransactionOperationV {

        void apply(ReadWriteTransaction txn) throws TransactionConflictException;

    }

    @FunctionalInterface
    private interface TransactionOperation<ReturnT> {

        ReturnT apply(AcquiresCacheMutator acm, ReadWriteTransaction txn) throws TransactionConflictException;

    }

    @FunctionalInterface
    private interface UncachedTransactionOperation<ReturnT> {

        ReturnT apply(ReadWriteTransaction txn) throws TransactionConflictException;

    }

}
