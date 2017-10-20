    package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.ContentionException;
import com.namazustudios.socialengine.rt.exception.DuplicateException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ConcurrentModificationException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.Thread.yield;

/**
 * A generic {@link ResourceService} which can take any type of {@link Resource}.
 *
 * This maps instances of {@link Resource} to their paths.
 *
 * This uses a simple locking strategy to lock paths in order to manipulate resources.  Note that the
 * entire resource isn't locked, but rather paths are locked separately.  Everything is stored
 * in memory.
 *
 * Note that a {@link Resource} must exist at one and only one {@link Path}.
 *
 * Created by patricktwohig on 8/4/15.
 */
public class SimpleResourceService implements ResourceService {

    private static final int RETRY_COUNT = 5;

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceService.class);

    private final AtomicReference<Storage<Resource>> storageAtomicReference = new AtomicReference<>(new Storage());

    private ResourceLockService resourceLockService;

    private ResourceIdLockService resourceIdLockService;

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
            } else if (resourceIdLockService.isLock(resourceId)) {
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
    public Path getPathForResourceId(ResourceId resourceId) {

        final Path path = storageAtomicReference.get().getResourceIdPathMap().get(resourceId);

        if (path == null) {
            throw new ResourceNotFoundException("No path for resource with ID: " + resourceId);
        }

        return path;
    }

    @Override
    public void addResource(final Path path, final Resource resource) {

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final ResourceId lock = getResourceIdLockService().createLock();

        doOptimisticV(() -> {

            final Storage<Resource> storage = storageAtomicReference.get();

            try {


                final ResourceId existing = storage.getPathResourceIdMap().putIfAbsent(path, lock);

                if (getResourceIdLockService().isLock(existing)) {
                    // If another thread is attempting to operate on the same path, then
                    // this is locked.  We need to fall back and try again after some fall
                    // off.
                    throw new LockedException();
                } else if (existing != null) {
                    // An actual resource is occupying this path.
                    throw new DuplicateException("Resource at path already exists " + path);
                } else if (storage.getResources().putIfAbsent(resource.getId(), resource) != null) {
                    // If that failed then we are attempting to insert this to separate paths.
                    throw new DuplicateException("Attempting to add already-existing resource to path." + path);
                }

                if (storage.getResourceIdPathMap().put(resource.getId(), path) != null) {
                    logger.error("Consistency Error:  {} is already mapped to path {}", resource.getId(), path);
                }

                // We now know that the resource has been inserted completely into the master resource
                // catalogue.  Time to complete the job by actually mapping the path properly.

                final ResourceId removed = storage.getPathResourceIdMap().put(path, resource.getId());

                if (!lock.equals(removed)) {
                    logger.error("Consistency Error:  Expected lock {} but got {}", lock, removed);
                }

            } finally {
                storage.getPathResourceIdMap().remove(path, lock);
            }

        });

    }

    @Override
    public Resource removeResource(final ResourceId resourceId) {

        return doOptimistic(() -> {

            final Storage<Resource> storage = storageAtomicReference.get();
            final Resource removed = storage.getResources().remove(resourceId);

            if (removed == null) {
                throw new ResourceNotFoundException("No resource with id " + resourceId);
            }

            final Path path = storage.getResourceIdPathMap().remove(resourceId);
            storage.getPathResourceIdMap().remove(path, resourceId);
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

    public ResourceIdLockService getResourceIdLockService() {
        return resourceIdLockService;
    }

    @Inject
    public void setResourceIdLockService(ResourceIdLockService resourceIdLockService) {
        this.resourceIdLockService = resourceIdLockService;
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

        private final ConcurrentMap<ResourceId, Path> resourceIdPathMap = new ConcurrentHashMap<>();

        public ConcurrentMap<ResourceId, T> getResources() {
            return resources;
        }

        public ConcurrentNavigableMap<Path, ResourceId> getPathResourceIdMap() {
            return pathResourceIdMap;
        }

        public ConcurrentMap<ResourceId, Path> getResourceIdPathMap() {
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
