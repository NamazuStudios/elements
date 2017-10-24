    package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.ContentionException;
import com.namazustudios.socialengine.rt.exception.DuplicateException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.Thread.yield;

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

    private final AtomicReference<Storage<Resource>> storageAtomicReference = new AtomicReference<>(new Storage());

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

            final Storage<Resource> storage = storageAtomicReference.get();
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

            final Storage<Resource> storage = storageAtomicReference.get();

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

                if (!resourceIdLock.equals(removed)) {
                    logger.error("Consistency Error:  Expected lock for {} but got {}", resourceIdLock, removed);
                }

                if (!pathLock.equals(oldPaths)) {
                    logger.error("Consistency Error:  Expected lock for {}. Got: {}", resourceId, oldPaths);
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
    public Collection<ResourceId> listResourceIdsMatching(Path path) {
        return null;
    }

    @Override
    public void linkResourceId(ResourceId sourceResourceId, Path destination) {

    }

    @Override
    public boolean unlinkPath(Path path, Consumer<ResourceId> removed) {
        return false;
    }

    @Override
    public Resource removeResource(final ResourceId resourceId) {

        return doOptimistic(() -> {

            final Storage<Resource> storage = storageAtomicReference.get();
            final Resource removed = storage.getResources().remove(resourceId);

            if (removed == null) {
                throw new ResourceNotFoundException("No resource with id " + resourceId);
            }

            final Deque<Path> pathDeque = storage.getResourceIdPathMap().remove(resourceId);
            pathDeque.forEach(path -> storage.getPathResourceIdMap().remove(path, resourceId));
            getResourceLockService().delete(resourceId);

            return removed;

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

    private static final class Storage<T> {


        private final ConcurrentMap<ResourceId, T> resources = new ConcurrentHashMap<>();

        private final ConcurrentNavigableMap<Path, ResourceId> pathResourceIdMap = new ConcurrentSkipListMap<>();

        private final ConcurrentMap<ResourceId, Deque<Path>> resourceIdPathMap = new ConcurrentHashMap<>();

        public ConcurrentMap<ResourceId, T> getResources() {
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
