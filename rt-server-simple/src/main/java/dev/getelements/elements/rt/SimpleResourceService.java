    package dev.getelements.elements.rt;

    import dev.getelements.elements.rt.exception.ContentionException;
    import dev.getelements.elements.rt.exception.DuplicateException;
    import dev.getelements.elements.rt.exception.InternalException;
    import dev.getelements.elements.rt.exception.ResourceNotFoundException;
    import dev.getelements.elements.rt.id.NodeId;
    import dev.getelements.elements.rt.id.ResourceId;
    import dev.getelements.elements.rt.util.FinallyAction;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    import javax.inject.Inject;
    import java.util.*;
    import java.util.concurrent.*;
    import java.util.concurrent.atomic.AtomicReference;
    import java.util.function.Consumer;
    import java.util.function.Supplier;
    import java.util.stream.Stream;

    import static java.lang.Thread.yield;
    import static java.util.Collections.emptyList;
    import static java.util.Spliterator.*;

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

    private static final int RETRY_COUNT = 250;

    private static final int MIN_WAIT = 1;

    private static final int MAX_WAIT = 100;

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceService.class);

    private final AtomicReference<Storage> storageAtomicReference = new AtomicReference<>(new Storage());

    private NodeId nodeId;

    private ResourceLockService resourceLockService;

    private OptimisticLockService<Deque<Path>> pathOptimisticLockService;

    private OptimisticLockService<ResourceId> resourceIdOptimisticLockService;

    @Override
    public boolean exists(final ResourceId resourceId) {
        return getStorage().getResources().containsKey(resourceId);
    }

    @Override
    public Resource getAndAcquireResourceWithId(final ResourceId resourceId) {

        check(resourceId);

        final Resource resource = getStorage().getResources().get(resourceId);

        if (resource == null) {
            throw new ResourceNotFoundException("Resource not found: " + resourceId);
        }

        return resource;

    }

    @Override
    public Resource getAndAcquireResourceAtPath(final Path path) {

        final var qualified = qualify(path);

        if (qualified.isWildcard()) {
            throw new IllegalArgumentException("Cannot fetch single resource with wildcard path " + qualified);
        }

        return doOptimistic(() -> {

            final var storage = getStorage();
            final var resourceId = storage.getPathResourceIdMap().get(qualified);

            if (resourceId == null) {
                throw new ResourceNotFoundException("Resource at path not found: " + qualified);
            } else if (getResourceIdOptimisticLockService().isLock(resourceId)) {
                throw new LockedException();
            }

            final var resource = storage.getResources().get(resourceId);

            if (resource == null) {
                throw new ResourceNotFoundException("Resource at path not found: " + qualified);
            }

            return resource;

        });

    }

    @Override
    public void addAndReleaseResource(final Path path, final Resource resource) {

        final var qualified = qualify(path);

        if (qualified.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final var resourceId = check(resource.getId());

        final var pathLock = getPathOptimisticLockService().createLock();
        final var resourceIdLock = getResourceIdOptimisticLockService().createLock();

        doOptimisticV(() -> {

            final var storage = getStorage();

            try {

                final var existingPaths = storage.getResourceIdPathMap().putIfAbsent(resourceId, pathLock);
                final var existingResourceId = storage.getPathResourceIdMap().putIfAbsent(qualified, resourceIdLock);

                if (getPathOptimisticLockService().isLock(existingPaths)) {
                    throw new LockedException("existing paths locked");
                } else if (getResourceIdOptimisticLockService().isLock(existingResourceId)) {
                    throw new LockedException("existing resource id locked");
                } else if (existingResourceId != null || existingPaths != null) {
                    // An actual resource is occupying this path.
                    throw new DuplicateException("Resource at path already exists: " + qualified);
                } else if (storage.getResources().putIfAbsent(resourceId, resource) != null) {
                    // While is is possible to insert the resource at multiple paths, this met
                    throw new DuplicateException("Attempting to add already-existing resource to path." + qualified);
                }

                // We now know that the resource has been inserted completely into the master resource mapping it's
                // Time to complete the job by actually mapping the path properly.

                final var newPaths = new ConcurrentLinkedDeque<Path>();
                newPaths.add(qualified);

                final var oldPaths = storage.getResourceIdPathMap().put(resourceId, newPaths);
                final var removed = storage.getPathResourceIdMap().put(qualified, resourceId);

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
                storage.getPathResourceIdMap().remove(qualified, resourceIdLock);
                storage.getResourceIdPathMap().remove(resourceId, pathLock);
            }

        });

    }

    @Override
    public Resource addAndAcquireResource(final Path path, final Resource resource) {
        final var qualified = qualify(path);
        addAndReleaseResource(qualified, resource);
        return resource;
    }

    @Override
    public Spliterator<Listing> list(final Path searchPath) {

        final var qualifiedSearchPath = qualify(searchPath);

        final Storage storage = getStorage();
        final Map<Path, ResourceId> tailMap = storage.getPathResourceIdMap().tailMap(qualifiedSearchPath);

        return new Spliterators.AbstractSpliterator<> (tailMap.size(), CONCURRENT | IMMUTABLE | NONNULL) {

            private final Iterator<Map.Entry<Path, ResourceId>> iterator = tailMap.entrySet().iterator();

            @Override
            public boolean tryAdvance(final Consumer<? super Listing> action) {

                if (!iterator.hasNext()) {
                    return false;
                }

                final Map.Entry<Path, ResourceId> pathResourceIdEntry = iterator.next();

                final Path path = pathResourceIdEntry.getKey();
                final ResourceId resourceId = pathResourceIdEntry.getValue();

                if (qualifiedSearchPath.matches(path)) {
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

        final var qualifiedDestination = qualify(destination);
        check(sourceResourceId);

        if (qualifiedDestination.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final Deque<Path> pathLock = getPathOptimisticLockService().createLock();
        final ResourceId resourceIdLock = getResourceIdOptimisticLockService().createLock();

        doOptimisticV(() -> {

            final Storage storage = getStorage();

            FinallyAction finallyAction = () -> {};

            try {

                final Deque<Path> existingPaths = storage.getResourceIdPathMap().replace(sourceResourceId, pathLock);
                finallyAction = finallyAction.then(() -> storage.getResourceIdPathMap().replace(sourceResourceId, pathLock, existingPaths));

                final ResourceId existingResourceId = storage.getPathResourceIdMap().putIfAbsent(qualifiedDestination, resourceIdLock);
                finallyAction = finallyAction.then(() -> storage.getPathResourceIdMap().remove(qualifiedDestination, resourceIdLock));

                if (getPathOptimisticLockService().isLock(existingPaths)) {
                    throw new LockedException("existing paths locked");
                } else if (getResourceIdOptimisticLockService().isLock(existingResourceId)) {
                    throw new LockedException("existing resource id locked");
                } else if (existingResourceId != null) {
                    throw new DuplicateException("Resource with id " + existingResourceId + " already exists at path " + qualifiedDestination);
                } else if (!storage.getResources().containsKey(sourceResourceId)) {
                    throw new ResourceNotFoundException("Resource with id " + sourceResourceId + "not found.");
                }

                // All locks are acquired, the following operations should never fail because all pre-conditions
                // are checked and all appropriate collections are locked.

                // Should not fail because simply adding a value to a collection should not cause an exception, unless
                // something is seriously wrong.
                existingPaths.add(qualifiedDestination);

                if (!storage.getPathResourceIdMap().replace(qualifiedDestination, resourceIdLock, sourceResourceId)) {
                    logger.error("Consistency error.  Could not link {} -> {}", sourceResourceId, qualifiedDestination);
                }

            } finally {
                finallyAction.run();
            }

        });
    }

    @Override
    public Unlink unlinkPath(final Path path, final Consumer<Resource> removed) {

        final var qualified = qualify(path);

        if (qualified.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final Deque<Path> pathLock = getPathOptimisticLockService().createLock();
        final ResourceId resourceIdLock = getResourceIdOptimisticLockService().createLock();

        return doOptimistic(() -> {

            FinallyAction finallyAction = () -> {};

            final Storage storage = getStorage();

            try {

                final ResourceId existingResourceId = storage.getPathResourceIdMap().replace(qualified, resourceIdLock);
                finallyAction = finallyAction.then(() -> {
                    if (existingResourceId == null) {
                        storage.getPathResourceIdMap().remove(qualified, resourceIdLock);
                    } else {
                        storage.getPathResourceIdMap().replace(qualified, resourceIdLock, existingResourceId);
                    }

                });

                if (existingResourceId == null) {
                    throw new ResourceNotFoundException("No resource at path " + qualified);
                } else if (getResourceIdOptimisticLockService().isLock(existingResourceId)) {
                    throw new LockedException("path locked");
                } else if (!storage.getResources().containsKey(existingResourceId)) {
                    logger.error("Conistency error.  No resource for id {}", existingResourceId);
                    throw new ResourceNotFoundException("No resource at path " + qualified);
                }

                final Deque<Path> existingPaths = storage.getResourceIdPathMap().replace(existingResourceId, pathLock);
                finallyAction = finallyAction.then(() -> storage.getResourceIdPathMap().replace(existingResourceId, pathLock, existingPaths));

                if (existingPaths == null) {
                    // This should never happen.  We throw an internal exception to indicate the failure because with
                    // a null value we cannot proceed.
                    logger.error("Consistency error, got null path for {} -> {} ", qualified, existingResourceId);
                    throw new InternalException("Got null paths for resource id " +  existingResourceId);
                } else if (getPathOptimisticLockService().isLock(existingPaths)) {
                    throw new LockedException("resource id locked");
                }

                if (!storage.getPathResourceIdMap().remove(qualified, resourceIdLock)) {
                    // This should never happen
                    logger.error("Consistency error, expected lock when removing path {}", qualified);
                }

                if (!existingPaths.remove(qualified)) {
                    // This should never happen
                    logger.error("Consistency error, bidirectional mapping broken for  {} -> {} ", qualified, existingResourceId);
                }

                final boolean isRemoved = existingPaths.isEmpty();

                if (isRemoved) {

                    final Resource resource = storage.getResources().remove(existingResourceId);

                    if (resource == null) {
                        logger.error("Consistency error, no resource for resource id {}", existingResourceId);
                    }

                    storage.getPathResourceIdMap().remove(qualified);
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
                finallyAction.run();
            }

        });

    }

    @Override
    public List<Unlink> unlinkMultiple(final Path path, final int max, final Consumer<Resource> removed) {
        logger.error("unlinkMultiple(Path, int, Consumer) not available for this implementation.");
        return emptyList();
    }

    @Override
    public Resource removeResource(final ResourceId resourceId) {

        final Deque<Path> pathLock = getPathOptimisticLockService().createLock();
        check(resourceId);

        return doOptimistic(() -> {

            final Storage storage = getStorage();

            FinallyAction finallyAction = () -> {};

            try {

                final Deque<Path> existingPaths = storage.getResourceIdPathMap().replace(resourceId, pathLock);
                finallyAction = finallyAction.then(() -> storage.getResourceIdPathMap().remove(resourceId, pathLock));

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

                    finallyAction.then(() -> {
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
                finallyAction.run();
            }

        });

    }

    @Override
    public List<ResourceId> removeResources(final Path path, final int max, final Consumer<Resource> removed) {
        logger.error("removeResources(Path, int, Consumer) not available for this implementation.");
        return emptyList();
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

    @Override
    public void stop() {

        final Storage storage = storageAtomicReference.getAndSet(null);

        storage.getResources().values().parallelStream().forEach(r -> {
            try {
                r.close();
            } catch (Exception ex) {
                logger.error("Error closing resource {}", r.getId(), ex);
            }
        });

    }

    private Storage getStorage() {
        final Storage storage = storageAtomicReference.get();
        if (storage == null) throw new IllegalStateException("closed");
        return storage;
    }

    private Path qualify(final Path path) {
        return path.toPathWithContext(nodeId.asString());
    }

    private ResourceId check(final ResourceId resourceId) {

        if (!nodeId.equals(resourceId.getNodeId())) {
            throw new IllegalArgumentException("Mismatching ResourceID");
        }

        return resourceId;

    }

    public NodeId getNodeId() {
        return nodeId;
    }

    @Inject
    public void setNodeId(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public long getInMemoryResourceCount() {
        return getStorage().getResources().size();
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
                    if (i < RETRY_COUNT / 2) {
                        doWait();
                    }
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
                    if (i < RETRY_COUNT / 2) {
                        doWait();
                    }
                }
            }

            throw new ContentionException();

        }

        private static void doWait() {

            final var tlr = ThreadLocalRandom.current();

            try {
                final var time = tlr.nextInt(MIN_WAIT, MAX_WAIT);
                Thread.sleep(time);
            } catch (InterruptedException ex) {
                throw new InternalException(ex);
            }

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
