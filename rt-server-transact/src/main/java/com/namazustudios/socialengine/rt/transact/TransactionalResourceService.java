package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.exception.ContentionException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Spliterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;
import static java.util.Spliterator.*;
import static java.util.Spliterators.spliterator;
import static java.util.stream.Collectors.toList;

public class TransactionalResourceService implements ResourceService {

    private static final int RETRY_COUNT = 32;

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
        try (var mutator = mutate(resourceId)) {
            return computeRO(txn -> {

                // The transaction is the authority. If there is no resource with the ID, then we simply
                // refuse all access to the resource.

                if (!txn.exists(resourceId)) {
                    throw new ResourceNotFoundException("Resource not found: " + resourceId);
                }

                if (mutator.isPresent()) {
                    // Cache hit. This means that the resource is stored in memory and is ready to
                    // be used by the calling code.
                    return mutator.acquire();
                } else {
                    // Cache miss. Load and return.
                    try (var rbc = txn.loadResourceContents(resourceId)) {
                        final var loaded = getResourceLoader().load(rbc, false);
                        return mutator.acquireInitial(loaded);
                    } catch (IOException e) {
                        throw new InternalException(e);
                    }
                }

            });
        }
    }

    @Override
    public Resource getAndAcquireResourceAtPath(final Path path) {
        final var resourceId = computeRO(txn -> txn.getResourceId(path));
        return getAndAcquireResourceWithId(resourceId);
    }

    @Override
    public Resource addAndAcquireResource(final Path path, final Resource resource) {
        try (var mutator = mutate(resource.getId())) {

            if (mutator.isPresent()) {
                throw new InternalException("Resource already present.");
            }

            final var destination = normalize(path).appendUUIDIfWildcard();
            executeRW((txn) -> txn.linkNewResource(destination, resource.getId()));

            return mutator.acquireInitial(resource);

        }
    }

    @Override
    public void addAndReleaseResource(final Path path, final Resource resource) {
        try (var mutator = mutate(resource.getId())) {
            executeRW(txn -> {

                final var destination = normalize(path).appendUUIDIfWildcard();

                try (var wbc = txn.saveNewResource(destination, resource.getId())) {
                    resource.serialize(wbc);
                } catch (IOException ex) {
                    throw new InternalException(ex);
                }

            });
        }
    }

    @Override
    public boolean tryRelease(final Resource resource) {
        try (var mutator = mutate(resource.getId())) {

            if (!(resource instanceof TransactionalResource) || !mutator.isPresent()) {
                return false;
            }

            return mutator.release(count -> computeRW((txn) -> {

                if (txn.exists(resource.getId())) {

                    if (count == 0) {
                        try (final var wbc = txn.updateResource(resource.getId())) {
                            resource.serialize(wbc);
                        } catch (IOException ex) {
                            throw new InternalException(ex);
                        }
                    }

                    return true;

                }

                return false;

            }));

        }
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

        final var unlink = computeRW((txn) -> {
            final var normalized = normalize(path);
            return txn.unlinkPath(normalized);
        });

        if (unlink.isRemoved()) {

            final var resourceId = unlink.getResourceId();

            try (final var mutator = mutate(resourceId)) {
                mutator.purge();
            }

        }

        return unlink;

    }

    @Override
    public List<Unlink> unlinkMultiple(final Path path, final int max, final Consumer<Resource> removed) {

        final var unlinks = computeRW(txn -> {
            final var normalized = normalize(path);
            return txn.unlinkMultiple(normalized, max);
        });

        try (final var mutator = mutate(null)) {
            mutator.purge();
        }

        for(var unlink : unlinks) {

            if (!unlink.isRemoved()) {
                continue;
            }

            final var resourceId = unlink.getResourceId();

            try (final var mutator = mutate(resourceId)) {
                mutator.purge();
            }

        }

        return unlinks;

    }

    @Override
    public Resource removeResource(final ResourceId resourceId) {
        try (var mutator = mutate(resourceId)) {
            try {
                executeRW((txn) -> txn.removeResource(resourceId));
                mutator.purge();
                return null;
            } catch (ResourceNotFoundException ex) {
                mutator.purge();
                throw ex;
            }
        }
    }

    @Override
    public List<ResourceId> removeResources(final Path path, final int max, final Consumer<Resource> removed) {

        final var resourceIDs = computeRW((txn) -> {
            final Path normalized = normalize(path);
            return txn.removeResources(normalized, max);
        });

        for (final var resourceId : resourceIDs) {
            try (var mutator = mutate(resourceId)) {
                mutator.purge();
            }
        }

        return resourceIDs;

    }
    @Override
    public Stream<Resource> removeAllResources() {
        try (var exclusiveMutator = getContext().cache.exclusive();
             var exclusiveTransaction = getPersistence().openExclusiveRW(getNodeId())) {
            exclusiveTransaction.truncate();
            return exclusiveMutator.clear();
        }
    }

    @Override
    public long getInMemoryResourceCount() {
        final Context context = getContext();
        return context.cache.size();
    }

    private Path normalize(final Path path) {
        return path.hasContext() ? path : path.toPathWithContext(getNodeId().asString());
    }

    private TransactionalResourceServiceCache.Mutator mutate(final ResourceId resourceId) {
        final var context = getContext();
        return context.cache.mutate(resourceId);
    }

    public void persist(final ResourceId resourceId) {
        try (var mutator = mutate(resourceId)) {
            executeRW((txn) -> {
                try (var wbc = txn.updateResource(resourceId)) {
                    final var tr = mutator.getResource();
                    tr.serialize(wbc);
                } catch (IOException ex) {
                    throw new InternalException(ex);
                }
            });
        }
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
        try (final var txn = getPersistence().openRO(getNodeId())) {
            return operation.apply(txn);
        }
    }

    private void executeRW(final TransactionOperationV operation) {

        for (int i = 0; i < RETRY_COUNT; ++i) {
            try (final var txn = getPersistence().openRW(getNodeId())) {
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

    private <T> T computeRW(final TransactionOperation<ReadWriteTransaction, T> operation) {

        for (int i = 0; i < RETRY_COUNT; ++i) {

            try (final var txn = getPersistence().openRW(getNodeId())) {
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

    private void randomWait(final int retry) {

        final Random random = ThreadLocalRandom.current();
        final int time = random.nextInt((retry + 1) * WAIT_INTERVAL);

        try {
            sleep(time);
        } catch (InterruptedException ex) {
            throw new InternalException(ex);
        }

    }

    private static class Context {

        private final TransactionalResourceServiceCache cache = new SkipListTransactionalResourceServiceCache();

    }

}
