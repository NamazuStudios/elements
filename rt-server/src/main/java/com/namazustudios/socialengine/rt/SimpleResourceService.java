package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.TooBusyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ConcurrentModificationException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
public class SimpleResourceService<ResourceT extends Resource> implements ResourceService<ResourceT> {

    private static final int RETRY_COUNT = 5;

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceService.class);

    private final AtomicReference<Storage<ResourceT>> storageAtomicReference = new AtomicReference<>(new Storage());

    private PathLockFactory pathLockFactory;

    @Override
    public ResourceT getResourceWithId(final ResourceId resourceId) {

        final ResourceT resource = storageAtomicReference.get().getResources().get(resourceId);

        if (resource == null) {
            throw new NotFoundException("Resource not found: " + resourceId);
        }

        return resource;

    }

    @Override
    public ResourceT getResourceAtPath(final Path path) {

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot fetch single resource with wildcard path " + path);
        }

        return doOptimistic(() -> {

            final Storage<ResourceT> storage = storageAtomicReference.get();
            final ResourceId resourceId = storage.getPathResourceIdMap().get(path);

            if (resourceId == null) {
                throw new NotFoundException("Resource at path not found: " + path);
            } else if (pathLockFactory.isLock(resourceId)) {
                throw new LockedException();
            }

            final ResourceT resource = storage.getResources().get(resourceId);

            if (resource == null) {
                throw new NotFoundException("Resource at path not found: " + path);
            }

            return resource;

        });

    }

    @Override
    public Path getPathForResourceId(ResourceId resourceId) {

        final Path path = storageAtomicReference.get().getResourceIdPathMap().get(resourceId);

        if (path == null) {
            throw new NotFoundException("No path for resource with ID: " + resourceId);
        }

        return path;
    }

    @Override
    public void addResource(final Path path, final ResourceT resource) {

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final ResourceId lock = getPathLockFactory().createLock();

        doOptimisticV(() -> {

            final Storage<ResourceT> storage = storageAtomicReference.get();

            try {

                final ResourceId existing = storage.getPathResourceIdMap().putIfAbsent(path, lock);

                if (getPathLockFactory().isLock(existing)) {
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
    public AtomicOperationTuple<ResourceT> addResourceIfAbsent(
            final Path path,
            final Supplier<ResourceT> resourceInitializer) {

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final ResourceId lock = getPathLockFactory().createLock();

        return doOptimistic(() -> {

            final Storage<ResourceT> storage = storageAtomicReference.get();

            try {

                final ResourceId existing = storage.getPathResourceIdMap().computeIfAbsent(path, p -> lock);

                if (!existing.equals(lock) && getPathLockFactory().isLock(existing)) {
                    // Failure.  The resource path is currently locked by another thread.  We back off
                    // and we continue as normal.
                    throw new LockedException();
                }

                if (existing.equals(lock)) {

                    // Success! We inserted a new value into the map because we actually managed to lock
                    // the path and fetch the path.  Now it's time to insmert the value that's supplied
                    // into the
                    final ResourceT resource = resourceInitializer.get();

                    if (storage.getResources().putIfAbsent(resource.getId(), resource) != null) {
                        // If that failed then we are attempting to insert this to separate paths.  This should
                        // almost never happen unless somebody is trying something sketchy trying to duplicate
                        // resources.
                        throw new DuplicateException("Attempting to add already-existing resource to path." + path);
                    }

                    if (storage.getResourceIdPathMap().put(resource.getId(), path) != null) {
                        logger.error("Consistency Error:  {} is already mapped to path {}", resource.getId(), path);
                    }

                    // Lastly link the path to the resource ID and vise-versa.  Thus completing the transaction.
                    storage.getResourceIdPathMap().put(resource.getId(), path);
                    storage.getPathResourceIdMap().put(path, resource.getId());
                    return new SimpleAtomicOperationTuple<>(true, resource);

                } else {

                    // Failure.  We are looking at an existing value.  Fetch it and report
                    // appropriately.  No need to dig further, and no need to invoke the
                    // supplier (and risk creating heavy resources).

                    final ResourceT resource = storage.getResources().get(existing);

                    if (resource == null) {
                        // Since this method implies addition, throwing a NotFoundException
                        // here doesn't make sense.  This could be happening because it's
                        // in the process of being removed or added.  In this case we aren't
                        // certain so we must force the cycle to reattempt.
                        throw new LockedException();
                    }

                    return new SimpleAtomicOperationTuple<>(false, resource);

                }

            } finally {
                storage.getPathResourceIdMap().remove(path, lock);
            }

        });

    }

    @Override
    public ResourceT removeResource(final Path path) {

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final ResourceId lock = getPathLockFactory().createLock();

        return doOptimistic(() -> {

            final Storage<ResourceT> storage = storageAtomicReference.get();

            final ResourceId existing = storage.getPathResourceIdMap().get(path);

            if (existing == null ) {
                // If we can't find something, then we know that there's simply
                // nothing here.  It may be in the process of being destroyed
                // or removed, but it may not.
                throw new NotFoundException("No resource at path: " + path);
            } else if (getPathLockFactory().isLock(existing)) {
                // Path is locked elsewhere.  We don't touch this resource because
                // we don't want to interfere with another ongoing operation.
                throw new LockedException();
            }

            // Attempt to actually lock the resource at the path so we have a chance
            // to actually carry out our operation.  Once this happens we must
            // proceed to completely remove the rest of the mapping.

            if (!storage.getPathResourceIdMap().replace(path, existing, lock)) {
                // If this fails we probably are going to assume the resource is
                // locked between the two operations to get and set.
                throw new LockedException();
            }

            // So at this point we know the existing value is valid and we also know
            // that this should map to an existing value.
            final ResourceT resource = storage.getResources().get(existing);

            if (resource == null) {
                throw new NotFoundException("No resource at path: " + path);
            }

            // Removes the mapping from both places as wlel as the temporary lock
            // we assigned to the path mapping.

            storage.getResources().remove(existing);
            storage.getResourceIdPathMap().remove(resource.getId(), path);
            storage.getPathResourceIdMap().remove(path, lock);

            return resource;

        });

    }

    @Override
    public Stream<ResourceT> removeAllResources() {
        // Removes everything and replaces with completely new structures
        // in one atomic swap.  The Remaining values will dealt with
        // appropriately through the returned stream.  However, this method
        // guarantees that the resources are removed atomically.
        final Storage storage = storageAtomicReference.getAndSet(new Storage());
        return storage.getResources().values().parallelStream();
    }

    public PathLockFactory getPathLockFactory() {
        return pathLockFactory;
    }

    @Inject
    public void setPathLockFactory(PathLockFactory pathLockFactory) {
        this.pathLockFactory = pathLockFactory;
    }

    private <T> T doOptimistic(final Supplier<T> supplier) {

        for (int i = 0; i < RETRY_COUNT; ++i) {
            try {
                return supplier.get();
            } catch (LockedException ex) {
                Thread.yield();
                continue;
            }
        }

        throw new TooBusyException();

    }

    private <T> T doOptimisticV(final Runnable runnable) {

        for (int i = 0; i < RETRY_COUNT; ++i) {
            try {
                runnable.run();
                break;
            } catch (LockedException ex) {
                Thread.yield();
                continue;
            }
        }

        throw new TooBusyException();

    }

    private static final class Storage<T> {


        private final ConcurrentMap<ResourceId, T> resources = new ConcurrentHashMap<>();

        private final ConcurrentNavigableMap<Path, ResourceId> pathResourceIdMap = new ConcurrentSkipListMap<>();

        private final ConcurrentNavigableMap<ResourceId, Path> resourceIdPathMap = new ConcurrentSkipListMap<>();

        public ConcurrentMap<ResourceId, T> getResources() {
            return resources;
        }

        public ConcurrentNavigableMap<Path, ResourceId> getPathResourceIdMap() {
            return pathResourceIdMap;
        }

        public ConcurrentNavigableMap<ResourceId, Path> getResourceIdPathMap() {
            return resourceIdPathMap;
        }

    }

    private static class SimpleAtomicOperationTuple<T extends Resource> implements AtomicOperationTuple<T> {

        private final boolean newlyAdded;
        private final T resource;

        public SimpleAtomicOperationTuple(boolean newlyAdded, T resource) {
            this.newlyAdded = newlyAdded;
            this.resource = resource;
        }

        @Override
        public boolean isNewlyAdded() {
            return newlyAdded;
        }

        @Override
        public T getResource() {
            return resource;
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
