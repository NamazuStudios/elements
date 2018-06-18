package com.namazustudios.socialengine.rt.xodus;

import com.google.common.base.Stopwatch;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.ResourceLockService.Monitor;
import com.namazustudios.socialengine.rt.exception.DuplicateException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.Integer.max;
import static java.util.Spliterator.CONCURRENT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static jetbrains.exodus.bindings.IntegerBinding.entryToInt;
import static jetbrains.exodus.bindings.IntegerBinding.intToEntry;
import static jetbrains.exodus.bindings.StringBinding.entryToString;
import static jetbrains.exodus.bindings.StringBinding.stringToEntry;
import static jetbrains.exodus.env.StoreConfig.WITHOUT_DUPLICATES;
import static jetbrains.exodus.env.StoreConfig.WITHOUT_DUPLICATES_WITH_PREFIXING;
import static jetbrains.exodus.env.StoreConfig.WITH_DUPLICATES;

public class XodusResourceService implements ResourceService, ResourceAcquisition {

    private static final int LIST_BATCH_SIZE = 100;

    private static final Logger logger = LoggerFactory.getLogger(XodusResourceService.class);

    /**
     * The name of the {@link Store} for storing the persistent data associated with {@link Resource} entries.  Since
     * {@link Resource} instances may not be capable of storage this store may not be a complete collection of all
     * {@link Resource} instances in the system because a {@link Resource} may be created and destroyed before it is
     * ever serialized.
     */
    public static final String STORE_RESOURCES = "resources";

    /**
     * The name of the {@link Store} for {@link Path} entries.  This links {@link Path} to {@link ResourceId} instances
     * and represents a comprehensive list of all {@link Path}s tracked by this {@link ResourceService}.
     */
    public static final String STORE_PATHS = "paths";

    /**
     * The name of the {@link Store} for reverse {@link Path} entries. This links {@link ResourceId} instances to many
     * {@link Path} instances and represents a comprehensive list of all {@link ResourceId}s tracked by this
     * {@link ResourceService}.  This table is the only collection that consistently stores the resource id and can be
     * used to test if the resource exists in the database or not.
     */
    public static final String STORE_PATHS_REVERSE = "paths_reverse";

    /**
     * The name of the {@link Store} which counts the number of acquires for each resource.  This contains a key to
     * number mapping.  This only stores values if the resource is acquired.  If not acquires exist, the acquire count
     * is removed entirely.
     */
    public static final String STORE_ACQUIRES = "acquires";

    /**
     * The acquire condition for use with the {@link Monitor} instance used to obtain access to the cached instance
     * of a {@link Resource}.
     */
    public static final String CONDITION_ACQUIRE = "xodus.cache.acquire";

    /**
     * Timeout for the acquire operation.  As a failsafe, we set a timeout to acquire a resource if waiting for
     * another thread to acquire it.
     */
    public static final long ACQUIRE_TIMEOUT_MS = MILLISECONDS.convert(5, SECONDS);

    private Environment environment;

    private ResourceLoader resourceLoader;

    private ResourceLockService resourceLockService;

    private final AtomicReference<XodusCacheStorage> xodusCacheStorageAtomicReference = new AtomicReference<>(new XodusCacheStorage());

    @Override
    public Resource getAndAcquireResourceWithId(final ResourceId resourceId) {
        checkOpen();
        return getEnvironment().computeInTransaction(txn -> doGetAndAcquireResource(txn, resourceId)).get();
    }

    private Supplier<XodusResource> doGetAndAcquireResource(final Transaction txn, final ResourceId resourceId) {
        final ByteIterable key = stringToEntry(resourceId.asString());
        final Supplier<XodusResource> xodusResourceSupplier = doGetAndAcquireResource(txn, key);
        return xodusResourceSupplier;
    }

    private Supplier<XodusResource> doGetAndAcquireResource(final Transaction txn, final ByteIterable resourceIdKey) {

        final Store reverse = openReversePaths(txn);

        // Check that there is at least one path pointing to the resource id.  If there is, then we can deal with
        // either fetching the resource from memory of from the persistent storage of resources on disk.

        if (reverse.get(txn, resourceIdKey) == null) {
            return () -> {
                final XodusCacheKey xodusCacheKey = new XodusCacheKey(resourceIdKey);
                throw new ResourceNotFoundException("Resource not found: " + xodusCacheKey.getResourceId());
            };
        }

        return doAcquireResource(txn, resourceIdKey);

    }

    private Supplier<XodusResource> doAcquireResource(final Transaction txn, final ByteIterable resourceIdKey) {

        final Store resources = openResources(txn);
        final int acquires = doAcquire(txn, resourceIdKey);
        final XodusCacheKey xodusCacheKey = new XodusCacheKey(resourceIdKey);

        // We return a supplier, because we wish to avoid repeat attempts to use the serializer to actually read it from
        // a blob of bytes.  We also want to avoid acquiring a lock during the transactional phase of the method so
        // returning a supplier will side-step that issue while allowining us to handle edge cases when contention among
        // the same resource may be high.

        if (acquires == 1) {
            // Only read the bytes if we absolutely need to.
            final ByteIterable value = resources.get(txn, resourceIdKey);
            if (value == null) throw new InternalException("Inconsistent state.  Newly acquired resource has no value.");
            return () -> loadAndCache(xodusCacheKey, value);
        } else {
            return () -> readFromCache(xodusCacheKey);
        }

    }

    private XodusResource readFromCache(final XodusCacheKey xodusCacheKey) {

        final XodusResource xodusResource = getStorage().getResourceIdResourceMap().get(xodusCacheKey);

        if (xodusResource != null) {
            return xodusResource;
        }

        // It is possible that the database was written before the first to acquire has written it to the cache, so
        // therefore we must wait and acquire it later using a condition variable supplied by the resource lock
        // service.  Only the first thread to acquire will insert it into the cache.

        final Stopwatch stopwatch = Stopwatch.createStarted();

        try (final Monitor monitor = getResourceLockService().getMonitor(xodusCacheKey.getResourceId())) {

            final Condition condition = monitor.getCondition(CONDITION_ACQUIRE);

            XodusResource xr = null;

            while (xr == null) {

                final long timeout = ACQUIRE_TIMEOUT_MS - stopwatch.elapsed(MILLISECONDS);
                if (timeout < 0) throw new InternalException("Resource acquire timeout: " + xodusCacheKey.getResourceId());

                condition.await(timeout, MILLISECONDS);
                xr = getStorage().getResourceIdResourceMap().get(xodusCacheKey);

            }

            return xr;

        } catch (InterruptedException ex) {
            throw new InternalException(ex);
        }

    }

    private XodusResource loadAndCache(final XodusCacheKey xodusCacheKey, final ByteIterable value) {

        // This locks and loads the resource.  Once it's inserted into the cache, the signal hits and any other
        // waiters will be notified and can fetch it from the cache.  If it happens to be deleted in the time that it
        // takes to acquire, the timeout will still hit for the resource and an exception will be raised.  This should
        // also not happen very often, but could happen if there's thread starvation or the system is under heavy load

        try (final Monitor monitor = getResourceLockService().getMonitor(xodusCacheKey.getResourceId())) {

            final Condition condition = monitor.getCondition(CONDITION_ACQUIRE);
            condition.signalAll();

            final int length = value.getLength();
            final byte[] bytes = value.getBytesUnsafe();

            try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes, 0, length)) {

                final XodusResource xodusResource = new XodusResource(getResourceLoader().load(bis));

                if (getStorage().getResourceIdResourceMap().put(xodusCacheKey, xodusResource) != null) {
                    logger.error("Consistency error.  Expecting no existing resource in cache.");
                }

                return xodusResource;

            } catch (IOException ex) {
                throw new InternalException(ex);
            }

        }
    }

    @Override
    public Resource getAndAcquireResourceAtPath(final Path path) {

        checkOpen();

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot fetch single resource with wildcard path " + path);
        }

        return getEnvironment().computeInTransaction(txn -> {

            final Store store = openPaths(txn);
            final ByteIterable pathKey = stringToEntry(path.toNormalizedPathString());
            final ByteIterable resourceIdKey = store.get(txn, pathKey);

            if (resourceIdKey == null) {
                throw new ResourceNotFoundException("Resource at path not found: " + path);
            }

            final Supplier<XodusResource> xodusResourceSupplier = doGetAndAcquireResource(txn, resourceIdKey);

            if (xodusResourceSupplier == null) {
                throw new ResourceNotFoundException("Resource at path not found: " + path);
            }

            return xodusResourceSupplier;

        }).get();

    }

    @Override
    public void addAndReleaseResource(final Path path, final Resource resource) {

        checkOpen();

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final XodusResource xodusResource = new XodusResource(resource);

        getEnvironment().computeInTransaction(txn -> {

            final Store paths = openPaths(txn);
            final ByteIterable pathKey = stringToEntry(path.toNormalizedPathString());
            final ByteIterable resourceIdKey = stringToEntry(resource.getId().asString());

            doLink(txn, paths, resourceIdKey, pathKey);
            return doReleaseResource(txn, xodusResource);

        }).run();

    }

    @Override
    public Resource addAndAcquireResource(final Path path, final Resource resource) {

        checkOpen();

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final ResourceId resourceId = resource.getId();

        return getEnvironment().computeInTransaction(txn -> {

            final Store paths = openPaths(txn);
            final ByteIterable pathKey = stringToEntry(path.toNormalizedPathString());
            final ByteIterable resourceIdKey = stringToEntry(resource.getId().asString());
            doLink(txn, paths, resourceIdKey, pathKey);

            int acquires = doAcquire(txn, resourceIdKey);
            if (acquires != 1) throw new IllegalStateException("Expecting newly acquired resource count of 1");

            return (Supplier<XodusResource>) () -> {
                try (final Monitor monitor = getResourceLockService().getMonitor(resourceId)) {
                    final XodusResource xodusResource = new XodusResource(resource);
                    final XodusCacheKey xodusCacheKey = new XodusCacheKey(resourceId);
                    final Condition condition = monitor.getCondition(CONDITION_ACQUIRE);
                    condition.signalAll();
                    getStorage().getResourceIdResourceMap().put(xodusCacheKey, xodusResource);
                    return xodusResource;
                }
            };

        }).get();

    }

    @Override
    public void release(final Resource resource) {

        checkOpen();

        final XodusResource xodusResource = checkXodusResource(resource);

        getEnvironment().computeInTransaction(txn -> {

            final Store reverse = openReversePaths(txn);
            final ByteIterable key = xodusResource.getXodusCacheKey().getKey();

            if (reverse.get(txn, key) == null) {
                throw new ResourceNotFoundException("Resource not part of this ResourceService " + xodusResource.getId());
            }

            return doReleaseResource(txn, xodusResource);

        }).run();

    }

    private int doRelease(final Transaction txn, final ByteIterable resourceIdKey) {

        final Store acquiresStore = openAcquires(txn);
        final ByteIterable acquiresByteIterable = acquiresStore.get(txn, resourceIdKey);

        if (acquiresByteIterable == null) {
            return 0;
        }

        final int acquires = entryToInt(acquiresByteIterable);

        if (acquires == 0) {
            logger.error("Consitency Error.  Stored acquires value of 0.");
        }

        if (acquires <= 1) {
            acquiresStore.delete(txn, resourceIdKey);
        } else {
            acquiresStore.put(txn, resourceIdKey, intToEntry(acquires - 1));
        }

        return max(0, acquires - 1);

    }

    private Runnable doReleaseResource(final Transaction txn,
                                       final XodusResource xodusResource) {

        final ByteIterable resourceIdKey = stringToEntry(xodusResource.getId().asString());
        final int acquires = doRelease(txn, resourceIdKey);

        if (acquires == 0) {

            // The resource is eligible for persistence, so we persist with the storage collection after the transaction
            // closes because the trasnaction may operate multiple times.

            final Store resources = openResources(txn);
            xodusResource.persist(txn, resources);

            return () -> {

                try (final Monitor monitor = getResourceLockService().getMonitor(xodusResource.getId())) {

                    final XodusResource removed;
                    removed = getStorage().getResourceIdResourceMap().remove(xodusResource.getXodusCacheKey());

                    if (removed != null && removed != xodusResource) {
                        // Somebody else is in the process of removing it, so lets' let that happen to avoid any memory
                        // corruption if at all possible.  However, this means the service is in an undefined state so
                        // that still indicates a potential error.
                        logger.error("Cached resource mismatch.");
                        return;
                    }

                    try {
                        // Ensure that it's closed once it's persisted to avoi memory leaks.
                        xodusResource.close();
                    } catch (Exception ex) {
                        logger.error("Could not close resource.", ex);
                    }

                }

            };

        } else {
            return () -> {
                // If possible, we may want to do some signaling through a condition variable?
                logger.trace("Nothing to be done releasing Resource.");
            };
        }

    }

    @Override
    public Spliterator<Listing> list(final Path path) {
        checkOpen();
        return new Spliterators.AbstractSpliterator<Listing>(Long.MAX_VALUE, CONCURRENT | IMMUTABLE | NONNULL) {

            private boolean done = false;

            private Path next = path.stripWildcard();

            private final Queue<Listing> batch = new LinkedList<>();

            @Override
            public boolean tryAdvance(Consumer<? super Listing> action) {

                if (done) return false;

                Listing listing = batch.poll();

                if (listing == null) {
                    nextBatch();
                    listing = batch.poll();
                }

                if (listing == null) {
                    done = true;
                    return false;
                }

                action.accept(listing);
                return true;

            }

            private void nextBatch() {

                if (next == null) return;

                getEnvironment().executeInReadonlyTransaction(txn -> {

                    final Store store = openPaths(txn);

                    try (final Cursor cursor = store.openCursor(txn)) {

                        ByteIterable key = stringToEntry(next.toNormalizedPathString());
                        ByteIterable value = cursor.getSearchKeyRange(key);

                        for (int i = 0; i < LIST_BATCH_SIZE && value != null; ++i) {

                            key = cursor.getKey();
                            value = cursor.getValue();

                            final XodusListing xodusListing = new XodusListing(key, value);

                            if (path.matches(xodusListing.getPath())) {
                                batch.offer(xodusListing);
                            } else {
                                break;
                            }

                            if (!cursor.getNext()) {
                                break;
                            }

                        }

                        next = cursor.getNext() ? new Path(entryToString(cursor.getKey())) : null;

                    }

                });
            }

        };

    }

    @Override
    public void link(final ResourceId sourceResourceId, final Path destination) {

        checkOpen();

        if (destination.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final ByteIterable resourceIdKey = stringToEntry(sourceResourceId.asString());
        final ByteIterable pathKey = stringToEntry(destination.toNormalizedPathString());

        getEnvironment().executeInTransaction(txn -> {

            final Store paths = openPaths(txn);
            final ByteIterable existing = paths.get(txn, pathKey);

                if (existing != null) {
                    final String existingResourceId = entryToString(resourceIdKey);
                    throw new DuplicateException("Resource with id " + existingResourceId + " already exists at path " + destination);
                }

            doLink(txn, paths, resourceIdKey, pathKey);

        });

    }

    private void doLink(final Transaction txn, final Store paths,
                        final ByteIterable resourceIdKey, final ByteIterable pathKey) {

        if (paths.get(txn, pathKey) != null) {
            throw new DuplicateException("Resources already exists at path {}" + entryToString(pathKey));
        }

        final Store reverse = openReversePaths(txn);
        paths.put(txn, pathKey, resourceIdKey);
        reverse.put(txn, resourceIdKey, pathKey);

    }

    private int doAcquire(final Transaction txn, final ByteIterable resourceIdKey) {

        final Store acquiresStore = openAcquires(txn);
        final ByteIterable acquiresByteIterable = acquiresStore.get(txn, resourceIdKey);

        if (acquiresByteIterable == null) {
            acquiresStore.put(txn, resourceIdKey, intToEntry(1));
            return 1;
        } else {
            final int acquires = entryToInt(acquiresByteIterable) + 1;
            acquiresStore.put(txn, resourceIdKey, intToEntry(acquires));
            return acquires;
        }

    }

    @Override
    public Unlink unlinkPath(final Path path, final Consumer<Resource> reovedResourceConsumer) {

        checkOpen();

        final ByteIterable pathKey = stringToEntry(path.toNormalizedPathString());

        final Unlink unlink = getEnvironment().computeInTransaction(txn -> {

            final Store paths = openPaths(txn);
            final Store reverse = openReversePaths(txn);
            final Store resources = openResources(txn);

            final ByteIterable resourceIdValue = paths.get(txn, pathKey);

            if (resourceIdValue == null) {
                throw new ResourceNotFoundException("No resource at path " + path);
            } else if (!paths.delete(txn, pathKey)) {
                final String resourceId = entryToString(resourceIdValue);
                logger.error("Consistency error.  Unable to unlink path {} -> {}", path, resourceId);
            }

            try (final Cursor cursor = reverse.openCursor(txn)) {
                if (!cursor.getSearchBoth(resourceIdValue, pathKey) || !cursor.deleteCurrent()) {
                    final String resourceId = entryToString(resourceIdValue);
                    logger.error("Consistency error.  Reverse mapping broken {} -> {}", path, resourceId);
                }
            }

            final boolean removed;
            final ResourceId resourceId = new ResourceId(entryToString(resourceIdValue));

            try (final Cursor cursor = reverse.openCursor(txn)) {
                removed = cursor.getSearchKey(resourceIdValue) == null;
            }

            if (removed) {
                resources.delete(txn, resourceIdValue);
            }

            return new Unlink() {
                @Override
                public ResourceId getResourceId() {
                    return resourceId;
                }

                @Override
                public boolean isRemoved() {
                    return removed;
                }
            };

        });

        if (unlink.isRemoved()) {
            try (final Monitor m = getResourceLockService().getMonitor(unlink.getResourceId())) {
                final XodusCacheStorage xodusCacheStorage = getStorage();
                final XodusCacheKey cacheKey = new XodusCacheKey(unlink.getResourceId());
                final XodusResource xodusResource = xodusCacheStorage.getResourceIdResourceMap().remove(cacheKey);
                reovedResourceConsumer.accept(xodusResource == null ? DeadResource.getInstance() : xodusResource.getDelegate());
            }
        }

        return unlink;

    }

    @Override
    public Resource removeResource(final ResourceId resourceId) {

        checkOpen();

        final ByteIterable resourceIdKey = stringToEntry(resourceId.asString());

        getEnvironment().executeInTransaction(txn -> doRemoveResource(txn, resourceIdKey));

        try (final Monitor m = getResourceLockService().getMonitor(resourceId)) {
            final XodusCacheKey cacheKey = new XodusCacheKey(resourceId);
            final XodusResource xodusResource = getStorage().getResourceIdResourceMap().remove(cacheKey);
            final Resource resource = xodusResource == null ? DeadResource.getInstance() : xodusResource.getDelegate();
            return resource;
        }

    }

    private void doRemoveResource(final Transaction txn, final ByteIterable resourceIdKey) {
        final Store paths = openPaths(txn);
        final Store reverse = openReversePaths(txn);
        final Store resources = openResources(txn);
        doRemoveResource(txn, resourceIdKey, paths, reverse, resources);
    }

    private void doRemoveResource(final Transaction txn,
                                  final ByteIterable resourceIdKey,
                                  final Store paths, final Store reverse, final Store resources) {

        try (final Cursor cursor = reverse.openCursor(txn)) {

            ByteIterable pathKey = cursor.getSearchKey(resourceIdKey);
            if (pathKey == null) {
                final String resourceId = entryToString(resourceIdKey);
                throw new ResourceNotFoundException("No resource with id " + resourceId);
            }

            while (pathKey != null) {

                if (!paths.delete(txn, pathKey)) {
                    final String path = entryToString(pathKey);
                    final String resourceId = entryToString(resourceIdKey);
                    logger.error("Consistency error.  Path mapping broken {} -> {}", path, resourceId);
                }

                if (!cursor.deleteCurrent()) {
                    final String path = entryToString(pathKey);
                    final String resourceId = entryToString(resourceIdKey);
                    logger.error("Consistency error.  Reverse mapping broken {} -> {}", path, resourceId);
                }

                if(cursor.getNextDup()) {
                    pathKey = cursor.getValue();
                } else {
                    break;
                }

            }

        }

        resources.delete(txn, resourceIdKey);

    }

    @Override
    public Stream<Resource> removeAllResources() {

        checkOpen();

        return getEnvironment().computeInExclusiveTransaction(txn -> {
            getEnvironment().truncateStore(STORE_RESOURCES, txn);
            getEnvironment().truncateStore(STORE_PATHS, txn);
            getEnvironment().truncateStore(STORE_PATHS_REVERSE, txn);

            final List<Stream<XodusResource>> streams = new ArrayList<>();
            final XodusCacheStorage xodusCacheStorage = new XodusCacheStorage();

            xodusCacheStorageAtomicReference.accumulateAndGet(xodusCacheStorage, (e, u) -> {
                // Probably overkill for this particular use case, but just in case there's somehow more writes than we
                // expece, we want to ensure we get everything that could be created accidentally or intentionally.  This
                // ensures that even if entries are added erroneously they're accumulated in the supplied list.
                if (e != null) streams.add(e.getResourceIdResourceMap().values().stream());
                return u;
            });

            return streams
                .stream()
                .flatMap(xrs -> xrs)
                .map(xr -> xr.getDelegate())
                .collect(toList()).stream();

        });

    }

    @Override
    public void close() {

        final List<Stream<XodusResource>> streams = new ArrayList<>();
        final AtomicReference<Boolean> cancel = new AtomicReference<>();

        xodusCacheStorageAtomicReference.getAndAccumulate(null, (e, u) -> {

            if (e == null) {
                cancel.set(true);
            } else {
                streams.add(e.getResourceIdResourceMap().values().stream());
            }

            return u;

        });

        if (cancel.get()) {
            // Somebody else attempted to close this service.
            return;
        }

        getEnvironment().computeInExclusiveTransaction(txn -> {

            // This is very critical that closing tries to force everything

            final Store paths = openPaths(txn);
            final Store reverse = openReversePaths(txn);
            final Store resources = openResources(txn);

            return streams.stream().flatMap(identity()).distinct().map(xr -> {

                try {
                    xr.persist(txn, resources);
                } catch (Exception ex) {
                    try {
                        logger.error("Caught exception persisting resource {}  Destroying..", xr.getId(), ex);
                        doRemoveResource(txn, xr.getXodusCacheKey().getKey(), paths, reverse, resources);
                    } catch (Exception _ex) {
                        logger.error("Caught exception destroying resource {}.", xr.getId(), _ex);
                    }
                }

                return xr;

            }).collect(toList());

        }).forEach(xr -> {
            try {
                xr.close();
            } catch (Exception ex) {
                logger.error("Caught exception closing Resource {}", xr.getId(), ex);
            }
        });

    }

    @Override
    public void acquire(final ResourceId resourceId) {
        getEnvironment().executeInTransaction(txn -> {

            final Store acquiresStore = openAcquires(txn);
            final ByteIterable resourceIdKey = stringToEntry(resourceId.asString());
            final ByteIterable value = acquiresStore.get(txn, resourceIdKey);

            // This is only called to increment the acquire count, so it may not need to actually manipulate the
            // cache.  Trying to increment the count otherwise is an error.
            if (value == null) {
                logger.warn("Attempting to acquire resource which has no acquires.");
                return;
            }

            final int acquires = entryToInt(value);
            acquiresStore.put(txn, resourceIdKey, intToEntry(acquires + 1));

        });
    }

    @Override
    public void release(final ResourceId resourceId) {
        final XodusCacheKey xodusCacheKey = new XodusCacheKey(resourceId);
        final XodusResource xodusResource = getStorage().getResourceIdResourceMap().get(xodusCacheKey);
        if (xodusResource == null) return;
        release(xodusResource);
    }

    private Store openResources(final Transaction txn) {
        return getEnvironment().openStore(STORE_RESOURCES, WITHOUT_DUPLICATES, txn);
    }

    private Store openPaths(final Transaction txn) {
        return getEnvironment().openStore(STORE_PATHS, WITHOUT_DUPLICATES_WITH_PREFIXING, txn);
    }

    private Store openReversePaths(final Transaction txn) {
        return getEnvironment().openStore(STORE_PATHS_REVERSE, WITH_DUPLICATES, txn);
    }

    private Store openAcquires(final Transaction txn) {
        return getEnvironment().openStore(STORE_ACQUIRES, WITHOUT_DUPLICATES, txn);
    }

    public Environment getEnvironment() {
        return environment;
    }

    @Inject
    public void setEnvironment(@Named(XodusResourceContext.RESOURCE_ENVIRONMENT) Environment environment) {
        this.environment = environment;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Inject
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private void checkOpen() {
        final XodusCacheStorage xodusCacheStorage = xodusCacheStorageAtomicReference.get();
        if (xodusCacheStorage == null) throw new IllegalStateException("XodusResourceService is closed.");
    }

    private XodusCacheStorage getStorage() {
        final XodusCacheStorage xodusCacheStorage = xodusCacheStorageAtomicReference.get();
        if (xodusCacheStorage == null) throw new IllegalStateException("XodusResourceService is closed.");
        return xodusCacheStorageAtomicReference.get();
    }

    public ResourceLockService getResourceLockService() {
        return resourceLockService;
    }

    @Inject
    public void setResourceLockService(ResourceLockService resourceLockService) {
        this.resourceLockService = resourceLockService;
    }

    private XodusResource checkXodusResource(final Resource resource) {
        try {
            return (XodusResource) resource;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("Not a Xodus managed Resource.", ex);
        }
    }

}
