package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.*;
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
import java.util.function.*;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.transact.TransactionalResource.getTombstone;
import static java.lang.Thread.sleep;
import static java.util.Spliterator.*;
import static java.util.Spliterators.spliterator;
import static java.util.stream.Collectors.toList;

public class TransactionalResourceService implements ResourceService {

    private static final int RETRY_COUNT = 256;

    private static final int WAIT_INTERVAL = 5;

    private static final Logger logger = LoggerFactory.getLogger(TransactionalResourceService.class);

    private NodeId nodeId;

    private ResourceLoader resourceLoader;

    private TransactionalResourceServicePersistence persistence;

    private final AtomicReference<Context> context = new AtomicReference<>();

    @Override
    public void start() {

        final var context = new Context();

        if (this.context.compareAndSet(null, context)) {
            logger.info("Started.");
        } else {
            throw new IllegalStateException("Already running.");
        }

    }

    @Override
    public void stop() {
        final var context = this.context.getAndSet(null);
        if (context == null) throw new IllegalStateException("Not currently running.");
    }

    @Override
    public boolean exists(final ResourceId resourceId) {
        return computeRO(txn -> txn.exists(resourceId));
    }

    @Override
    public Resource getAndAcquireResourceWithId(final ResourceId resourceId) {
        return computeRO((acm, txn) -> {
            try {
                if (!txn.exists(resourceId)) throw new ResourceNotFoundException();
                return acm.acquire(resourceId);
            } catch (NullResourceException ex) {
                throw new TransactionConflictException();
            }
        });
    }

    @Override
    public Resource getAndAcquireResourceAtPath(final Path path) {
        return computeRO((acm, txn) -> {
            try {
                final Path normalized = normalize(path);
                final ResourceId resourceId = txn.getResourceId(normalized);
                return acm.acquire(resourceId);
            } catch (NullResourceException ex) {
                throw new TransactionConflictException();
            }
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

        final TransactionalResource tr;

        try {
            tr = (TransactionalResource) resource;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("Resource not owned by this ResourceService", ex);
        }

        final boolean result = computeRW((acm, txn) -> {

            if (!txn.exists(tr.getId()))
                return false;

            try (final WritableByteChannel wbc = txn.updateResource(tr.getId())) {

                if (tr.isNascent())
                    throw new IllegalArgumentException("Resource should not be nascent.");

                final int acquires = tr.acquireAndGet();
                acm.releaseOnClose(tr);

                if (acquires == 1) {
                    throw new IllegalStateException("This should never happen.");
                } else if (acquires == 2) {
                    tr.serialize(wbc);
                }

                return true;

            } catch (IOException ex) {
                throw new InternalException(ex);
            }

        });

        if (result) tr.release();
        return result;

    }

    @Override
    public Spliterator<Listing> list(final Path path) {
        return computeRO(txn -> {
            final var normalized = normalize(path);
            final var listings = txn.list(normalized).collect(toList());
            return spliterator(listings, SUBSIZED | CONCURRENT | NONNULL);
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
                if (unlink.isRemoved()) acm.evict(resourceId, removed);
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
            // Atomically, we replace the resource with the tombstone object, loading the latest revision of
            // the resource if it was not present in the cache.

            final var evicted = acm.tombstone(resourceId);

            // Removes this resource from the transactional storage. This method may throw a conflict exception.
            txn.removeResource(resourceId);

            // If the code has made it this far, we know the transaction will succeed. Therefore, we set aside the
            // destruction of the resource in the acm instance.
            acm.keep(evicted);

            if (!acm.clearTombstone(resourceId)) {
                // This would happen because another process has aggressively reloaded the resource while the final
                // removal didn't actually take place.
                logger.debug("Expected to have removed tombstone.");
            }

            return evicted;

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
        try (final ExclusiveReadWriteTransaction txn = getPersistence().openExclusiveRW(getNodeId())) {
            final Context old = this.context.getAndSet(new Context());
            if (old == null) throw new IllegalStateException("Not running.");
            txn.truncate();
            return old.acquires.values().stream().map(TransactionalResource::getDelegate);
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
        } catch (Exception ex) {
            logger.error("Caught exception.", ex);
            throw ex;
        }
    }

    private <T> T computeRO(final TransactionOperation<ReadOnlyTransaction, T> operation) {

        final Context context = getContext();

        for (int i = 0; i < RETRY_COUNT; ++i) {
            try (final ReadOnlyTransaction txn = getPersistence().openRO(getNodeId());
                 final AcquiresCacheMutator acm = new AcquiresCacheMutator(context, txn)) {
                final T value = operation.apply(acm, txn);
                return value;
            } catch (TransactionConflictException ex) {
                randomWait(i);
                continue;
            } catch (Exception ex) {
                logger.error("Caught exception.", ex);
                throw ex;
            }
        }

        throw new ContentionException();

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
            } catch (Exception ex) {
                logger.error("Caught exception.", ex);
                throw ex;
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
            } catch (Exception ex) {
                logger.error("Caught exception.", ex);
                throw ex;
            }
        }

        throw new ContentionException();

    }

    private <T> T computeRW(final UncachedTransactionOperation<T> operation) {

        for (int i = 0; i < RETRY_COUNT; ++i) {
            try (final ReadWriteTransaction txn = getPersistence().openRW(getNodeId())) {
                final T value = operation.apply(txn);
                txn.commit();
                return value;
            } catch (TransactionConflictException ex) {
                randomWait(i);
            } catch (Exception ex) {
                logger.error("Caught exception.", ex);
                throw ex;
            }
        }

        throw new ContentionException();

    }

    private <T> T computeRW(final TransactionOperation<ReadWriteTransaction, T> operation) {

        final Context context = getContext();

        for (int i = 0; i < RETRY_COUNT; ++i) {

            try (final ReadWriteTransaction txn = getPersistence().openRW(getNodeId());
                 final AcquiresCacheMutator acm = new AcquiresCacheMutator(context, txn)) {
                final T value = operation.apply(acm, txn);
                txn.commit();
                return value;
            } catch (TransactionConflictException ex) {
                randomWait(i);
            } catch (Exception ex) {
                logger.error("Caught exception.", ex);
                throw ex;
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

        private void purge(final TransactionalResource transactionalResource) {

            if (acquires.remove(transactionalResource.getId(), transactionalResource)) {

                logger.trace("Resource {} previously evicted", transactionalResource.getId());

                try {
                    transactionalResource.unload();
                } catch (Exception ex) {
                    logger.error("Error closing resource {}", transactionalResource, ex);
                }

            }

        }

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

        private final List<Resource> toUnload = new LinkedList<>();

        private final List<TransactionalResource> toClose = new LinkedList<>();

        private final List<TransactionalResource> toAcquire = new LinkedList<>();

        private final List<TransactionalResource> toRelease = new LinkedList<>();

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
                if (!r.acquire()) logger.error("Failed to acquire {} in post-acquire loop.", r);
            });

            toRelease.forEach(r -> {
                // This should also never fail because of the same reason above.  Each of the acquires happens before
                // this, so even any overlaps should have been acquired.  Any of these will be purged after the
                // reference count hits zero.

                try {
                    r.release();
                } catch (Exception ex) {
                    logger.warn("Caught exception releasing resource.", ex);
                }

            });

            toClose.forEach(r -> {
                try {
                    // We should only allow nascent-state resources to force close. acquire/release should handle all
                    // other cases as the resources specified here were extraneously created and just need to be
                    // destroyed.
                    if (!r.isNascent()) logger.error("Forcibly closing non-nascent Resource {}", r);
                    r.close();
                } catch (Exception ex) {
                    logger.error("Caught exception closing resource {}", r, ex);
                }
            });

            toUnload.forEach(r -> {
                try {
                    r.unload();
                } catch (Exception ex) {
                    logger.error("Caught exception closing resource {}", r, ex);
                }
            });

        }

        private TransactionalResource acquire(final Resource resource) {

            final ResourceId resourceId = resource.getId();

            final TransactionalResource transactionalResource = new TransactionalResource(resource, context::purge);
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
                } else {
                    // Since we have conservatively acquired the above resource, we must ensure that it will be
                    // released when the operation completes.
                    releaseOnClose(existing);
                    return existing;
                }
            });

            // We must make sure that it won't be closed with this cache manipulation.  This is side-effect free if we
            // didn't ever allocate a new resource in the first place.
            keep(result);
            acquireOnClose(result);

            return result;

        }

        private TransactionalResource loadTransactionalResource(final ResourceId resourceId) {
            try (final ReadableByteChannel rbc = txn.loadResourceContents(resourceId)) {

                final Resource resource = getResourceLoader().load(rbc, false);

                final TransactionalResource transactionalResource;
                transactionalResource = new TransactionalResource(resource, context::purge);
                transactionalResource.acquire();

                toClose.add(transactionalResource);
                return transactionalResource;

            } catch (IOException e) {
                throw new InternalException(e);
            }
        }

        private void keep(final Resource resource) {
            toClose.removeIf(r -> r == resource);
            toUnload.removeIf(r -> r == resource);
        }

        private void acquireOnClose(final TransactionalResource transactionalResource) {
            toAcquire.add(transactionalResource);
        }

        private void releaseOnClose(final TransactionalResource transactionalResource) {
            toRelease.add(transactionalResource);
        }

        public Resource tombstone(final ResourceId resourceId) {

            // We replace the value with the tombstone. This ensures that other fetches of this particular resource
            // will see it as removed.

            final var tr = context.acquires.replace(resourceId, getTombstone());

            // If there was nothing, or we had previously set the tombstone, then we will re-load it with the current
            // revision so that it's state (including destructors) can be executed as part of the process. The resource
            // is flagged for unloading when this ACM instance closes as not to leak memory.

            if (tr == null || tr.isTombstone()) {
                try (final ReadableByteChannel rbc = txn.loadResourceContents(resourceId)) {
                    final var loaded = getResourceLoader().load(rbc);
                    toUnload.add(loaded);
                    return loaded;
                } catch (IOException x) {
                    throw new InternalException(x);
                } catch (NullResourceException ex) {
                    return DeadResource.getInstance();
                }
            } else {
                var delegate = tr.getDelegate();
                toUnload.add(delegate);
                return delegate;
            }

        }

        public boolean clearTombstone(final ResourceId resourceId) {
            return context.acquires.remove(resourceId, getTombstone());
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
    private interface TransactionOperation<TransactionT extends ReadOnlyTransaction, ReturnT> {

        ReturnT apply(AcquiresCacheMutator acm, TransactionT txn) throws TransactionConflictException;

    }

    @FunctionalInterface
    private interface UncachedTransactionOperation<ReturnT> {

        ReturnT apply(ReadWriteTransaction txn) throws TransactionConflictException;

    }

}

