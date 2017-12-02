    package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.ContentionException;
import com.namazustudios.socialengine.rt.exception.DuplicateException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.util.FinallyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Thread.yield;
import static java.util.Spliterator.CONCURRENT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;

    /**
 * A {@link ResourceService} which stores instances of {@link Resource} instances in memory and indexes them by path.
 * This {@link ResourceService} uses an optimistic locking strategy and will perform best when avoiding path collisions.
 *
 * Like other similar approaches, this relies on eventual consistency.  In the event of very rapid reads and writes it
 * may be possible to miss links between paths and {@link Resource} instances.
 *
 * Created by patricktwohig on 8/4/15.
 */
public class SimpleResourceService implements ResourceService {

    private static final int RETRY_COUNT = 5;

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceService.class);

    private final AtomicReference<Storage> storageAtomicReference = new AtomicReference<>(new Storage());

    private ResourceLockService resourceLockService;

    private OptimisticLockService<Deque<Path>> pathOptimisticLockService;

    private OptimisticLockService<ResourceId> resourceIdOptimisticLockService;

    @Override
    public Resource getResourceWithId(final ResourceId resourceId) {

        final Resource resource = storageAtomicReference.get().getResources().get(resourceId);

        if (resource == null) {
            throw new ResourceNotFoundException("Resource not found: " + resourceId);
        }

        return resource;

    }

    @Override
    public Resource getResourceAtPath(final Path path) {

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot fetch single resource with wildcard path " + path);
        }

        return doOptimistic(() -> {

            final Storage storage = storageAtomicReference.get();
            final ResourceId resourceId = storage.getPathResourceIdMap().get(path);

            if (resourceId == null) {
                throw new ResourceNotFoundException("Resource at path not found: " + path);
            } else if (getResourceIdOptimisticLockService().isLock(resourceId)) {
                throw new LockedException();
            }

            final Resource resource = storage.getResources().get(resourceId);

            if (resource == null) {
                throw new ResourceNotFoundException("Resource at path not found: " + path);
            }

            return resource;

        });

    }

    @Override
    public void addResource(final Path path, final Resource resource) {

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final ResourceId resourceId = resource.getId();

        final Deque<Path> pathLock = getPathOptimisticLockService().createLock();
        final ResourceId resourceIdLock = getResourceIdOptimisticLockService().createLock();

        doOptimisticV(() -> {

            final Storage storage = storageAtomicReference.get();

            try {

                final Deque<Path> existingPaths = storage.getResourceIdPathMap().putIfAbsent(resourceId, pathLock);
                final ResourceId existingResourceId = storage.getPathResourceIdMap().putIfAbsent(path, resourceIdLock);

                if (getPathOptimisticLockService().isLock(existingPaths)) {
                    throw new LockedException("existing paths locked");
                } else if (getResourceIdOptimisticLockService().isLock(existingResourceId)) {
                    throw new LockedException("existing resource id locked");
                } else if (existingResourceId != null || existingPaths != null) {
                    // An actual resource is occupying this path.
                    throw new DuplicateException("Resource at path already exists: " + path);
                } else if (storage.getResources().putIfAbsent(resourceId, resource) != null) {
                    // While is is possible to insert the resource at multiple paths, this met
                    throw new DuplicateException("Attempting to add already-existing resource to path." + path);
                }

                // We now know that the resource has been inserted completely into the master resource mapping it's
                // Time to complete the job by actually mapping the path properly.

                final Deque<Path> newPaths = new ConcurrentLinkedDeque<>();
                newPaths.add(path);

                final Deque<Path> oldPaths = storage.getResourceIdPathMap().put(resourceId, newPaths);
                final ResourceId removed = storage.getPathResourceIdMap().put(path, resourceId);

                if (!pathLock.equals(oldPaths)) {
                    logger.error("Consistency Error:  Expected lock for {}. Got: {}", resourceId, oldPaths);
                }

                if (!resourceIdLock.equals(removed)) {
                    logger.error("Consistency Error:  Expected lock for {} but got {}", resourceIdLock, removed);
                }

            } finally {
                // These should be no-op unless there's a locking failure.  In which case, these will only roll back
                // locking because they would have been inserted with putIfAbsent, and they're only removed if they
                // currently equal the specific lock used in the locking strategy.
                storage.getPathResourceIdMap().remove(path, resourceIdLock);
                storage.getResourceIdPathMap().remove(resourceId, pathLock);
            }

        });

    }

    @Override
    public Spliterator<Listing> list(final Path searchPath) {

        final Storage storage = storageAtomicReference.get();
        final Map<Path, ResourceId> tailMap = storage.getPathResourceIdMap().tailMap(searchPath);

        return new Spliterators.AbstractSpliterator<Listing> (tailMap.size(), CONCURRENT | IMMUTABLE | NONNULL) {

            private final Iterator<Map.Entry<Path, ResourceId>> iterator = tailMap.entrySet().iterator();

            @Override
            public boolean tryAdvance(final Consumer<? super Listing> action) {

                if (!iterator.hasNext()) {
                    return false;
                }

                final Map.Entry<Path, ResourceId> pathResourceIdEntry = iterator.next();

                final Path path = pathResourceIdEntry.getKey();
                final ResourceId resourceId = pathResourceIdEntry.getValue();

                if (searchPath.matches(path)) {
                    action.accept(new Listing() {

                        @Override
                        public Path getPath() {
                            return path;
                        }

                        @Override
                        public ResourceId getResourceId() {
                            return resourceId;
                        }
                    });
                    return true;
                } else {
                    return false;
                }

            }

        };

    }

    @Override
    public void link(final ResourceId sourceResourceId, final Path destination) {

        if (destination.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final Deque<Path> pathLock = getPathOptimisticLockService().createLock();
        final ResourceId resourceIdLock = getResourceIdOptimisticLockService().createLock();

        doOptimisticV(() -> {

            final Storage storage = storageAtomicReference.get();

            FinallyAction finallyAction = () -> {};

            try {

                final Deque<Path> existingPaths = storage.getResourceIdPathMap().replace(sourceResourceId, pathLock);
                finallyAction = finallyAction.andThen(() -> storage.getResourceIdPathMap().replace(sourceResourceId, pathLock, existingPaths));

                final ResourceId existingResourceId = storage.getPathResourceIdMap().putIfAbsent(destination, resourceIdLock);
                finallyAction = finallyAction.andThen(() -> storage.getPathResourceIdMap().remove(destination, resourceIdLock));

                if (getPathOptimisticLockService().isLock(existingPaths)) {
                    throw new LockedException("existing paths locked");
                } else if (getResourceIdOptimisticLockService().isLock(existingResourceId)) {
                    throw new LockedException("existing resource id locked");
                } else if (existingResourceId != null) {
                    throw new DuplicateException("Resource with id " + existingResourceId + " already exists at path " + destination);
                } else if (!storage.getResources().containsKey(sourceResourceId)) {
                    throw new ResourceNotFoundException("Resource with id " + sourceResourceId + "not found.");
                }

                // All locks are acquired, the following operations should never fail because all pre-conditions
                // are checked and all appropriate collections are locked.

                // Should not fail because simply adding a value to a collection should not cause an exception, unless
                // something is seriously wrong.
                existingPaths.add(destination);

                if (!storage.getPathResourceIdMap().replace(destination, resourceIdLock, sourceResourceId)) {
                    logger.error("Consistency error.  Could not link {} -> {}", sourceResourceId, destination);
                }

            } finally {
                finallyAction.perform();
            }

        });
    }

    @Override
    public Unlink unlinkPath(final Path path, final Consumer<Resource> removed) {

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final Deque<Path> pathLock = getPathOptimisticLockService().createLock();
        final ResourceId resourceIdLock = getResourceIdOptimisticLockService().createLock();

        return doOptimistic(() -> {

            FinallyAction finallyAction = () -> {};

            final Storage storage = storageAtomicReference.get();

            try {

                final ResourceId existingResourceId = storage.getPathResourceIdMap().replace(path, resourceIdLock);
                finallyAction = finallyAction.andThen(() -> {
                    if (existingResourceId == null) {
                        storage.getPathResourceIdMap().remove(path, resourceIdLock);
                    } else {
                        storage.getPathResourceIdMap().replace(path, resourceIdLock, existingResourceId);
                    }

                });

                if (existingResourceId == null) {
                    throw new ResourceNotFoundException("No resource at path " + path);
                } else if (getResourceIdOptimisticLockService().isLock(existingResourceId)) {
                    throw new LockedException("path locked");
                } else if (!storage.getResources().containsKey(existingResourceId)) {
                    logger.info("Conistency error.  No resource for id {}", existingResourceId);
                    throw new ResourceNotFoundException("No resource at path " + path);
                }

                final Deque<Path> existingPaths = storage.getResourceIdPathMap().replace(existingResourceId, pathLock);
                finallyAction = finallyAction.andThen(() -> storage.getResourceIdPathMap().replace(existingResourceId, pathLock, existingPaths));

                if (existingPaths == null) {
                    // This should never happen.  We throw an internal exception to indicate the failure because with
                    // a null value we cannot proceed.
                    logger.error("Consistency error, got null path for {} -> {} ", path, existingResourceId);
                    throw new InternalException("Got null paths for resource id " +  existingResourceId);
                } else if (getPathOptimisticLockService().isLock(existingPaths)) {
                    throw new LockedException("resource id locked");
                }

                if (!storage.getPathResourceIdMap().remove(path, resourceIdLock)) {
                    // This should never happen
                    logger.error("Consistency error, expected lock when removing path {}", path);
                }

                if (!existingPaths.remove(path)) {
                    // This should never happen
                    logger.error("Consistency error, bidirectional mapping broken for  {} -> {} ", path, existingResourceId);
                }

                final boolean isRemoved = existingPaths.isEmpty();

                if (isRemoved) {

                    final Resource resource = storage.getResources().remove(existingResourceId);

                    if (resource == null) {
                        logger.error("Consistency error, no resource for resource id {}", existingResourceId);
                    }

                    storage.getPathResourceIdMap().remove(path);
                    storage.getResourceIdPathMap().remove(existingResourceId);

                    removed.accept(resource);

                }

                return new Unlink() {
                    @Override
                    public ResourceId getResourceId() {
                        return existingResourceId;
                    }

                    @Override
                    public boolean isRemoved() {
                        return isRemoved;
                    }
                };

            } finally {
                finallyAction.perform();
            }

        });

    }

    @Override
    public Resource removeResource(final ResourceId resourceId) {

        final Deque<Path> pathLock = getPathOptimisticLockService().createLock();

        return doOptimistic(() -> {

            final Storage storage = storageAtomicReference.get();

            FinallyAction finallyAction = () -> {};

            try {

                final Deque<Path> existingPaths = storage.getResourceIdPathMap().replace(resourceId, pathLock);
                finallyAction = finallyAction.andThen(() -> storage.getResourceIdPathMap().remove(resourceId, pathLock));

                if (existingPaths == null) {
                    throw new ResourceNotFoundException("no resource for id " + resourceId);
                } else if (getPathOptimisticLockService().isLock(existingPaths)) {
                    throw new LockedException("resource id locked");
                }

                for (final Path path : existingPaths) {

                    final ResourceId resourceIdLock = getResourceIdOptimisticLockService().createLock();
                    final ResourceId existingResourceId = storage.getPathResourceIdMap().replace(path, resourceIdLock);

                    if (!Objects.equals(resourceId, existingResourceId)) {
                        logger.error("Consistency error, bidirectional mapping broken for {} -> {}", path, resourceId);
                    }

                    finallyAction.andThen(() -> {
                        if (existingResourceId == null) {
                            storage.getPathResourceIdMap().remove(path, resourceIdLock);
                        } else {
                            storage.getPathResourceIdMap().replace(path, resourceIdLock, existingResourceId);
                        }
                    });

                    if (getResourceIdOptimisticLockService().isLock(existingResourceId)) {
                        throw new LockedException("locked at path");
                    }

                }

                final Resource removed = storage.getResources().remove(resourceId);

                if (removed == null) {
                    throw new ResourceNotFoundException("No resource with id " + resourceId);
                }

                storage.getResourceIdPathMap().remove(resourceId);
                existingPaths.forEach(path -> storage.getPathResourceIdMap().remove(path));
                getResourceLockService().delete(resourceId);

                return removed;

            } finally {
                finallyAction.perform();
            }

        });

    }

    @Override
    public Stream<Resource> removeAllResources() {
        // Removes everything and replaces with completely new structures
        // in one atomic swap.  The Remaining values will dealt with
        // appropriately through the returned stream.  However, this method
        // guarantees that the resources are removed atomically.
        final Storage storage = storageAtomicReference.getAndSet(new Storage());
        return storage.getResources().values().parallelStream();
    }

    public ResourceLockService getResourceLockService() {
        return resourceLockService;
    }

    @Inject
    public void setResourceLockService(ResourceLockService resourceLockService) {
        this.resourceLockService = resourceLockService;
    }

    public OptimisticLockService<Deque<Path>> getPathOptimisticLockService() {
        return pathOptimisticLockService;
    }

    @Inject
    public void setPathOptimisticLockService(OptimisticLockService<Deque<Path>> pathOptimisticLockService) {
        this.pathOptimisticLockService = pathOptimisticLockService;
    }

    public OptimisticLockService<ResourceId> getResourceIdOptimisticLockService() {
        return resourceIdOptimisticLockService;
    }

    @Inject
    public void setResourceIdOptimisticLockService(OptimisticLockService<ResourceId> resourceIdOptimisticLockService) {
        this.resourceIdOptimisticLockService = resourceIdOptimisticLockService;
    }

    private <T> T doOptimistic(final Supplier<T> supplier) {

        for (int i = 0; i < RETRY_COUNT; ++i) {
            try {
                return supplier.get();
            } catch (LockedException ex) {
                yield();
                continue;
            }
        }

        throw new ContentionException();

    }

    private void doOptimisticV(final Runnable runnable) {

        for (int i = 0; i < RETRY_COUNT; ++i) {
            try {
                runnable.run();
                return;
            } catch (LockedException ex) {
                yield();
                continue;
            }
        }

        throw new ContentionException();

    }

    private static final class Storage {


        private final ConcurrentMap<ResourceId, Resource> resources = new ConcurrentHashMap<>();

        private final ConcurrentNavigableMap<Path, ResourceId> pathResourceIdMap = new ConcurrentSkipListMap<>();

        private final ConcurrentMap<ResourceId, Deque<Path>> resourceIdPathMap = new ConcurrentHashMap<>();

        public ConcurrentMap<ResourceId, Resource> getResources() {
            return resources;
        }

        public ConcurrentNavigableMap<Path, ResourceId> getPathResourceIdMap() {
            return pathResourceIdMap;
        }

        public ConcurrentMap<ResourceId, Deque<Path>> getResourceIdPathMap() {
            return resourceIdPathMap;
        }

    }

    private static class LockedException extends ConcurrentModificationException {

        public LockedException() {}

        public LockedException(String message) {
            super(message);
        }

        public LockedException(Throwable cause) {
            super(cause);
        }

        public LockedException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
