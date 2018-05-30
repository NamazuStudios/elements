package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.*;
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
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Spliterator.CONCURRENT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.function.Function.identity;
import static jetbrains.exodus.bindings.StringBinding.entryToString;
import static jetbrains.exodus.bindings.StringBinding.stringToEntry;
import static jetbrains.exodus.env.StoreConfig.WITHOUT_DUPLICATES;
import static jetbrains.exodus.env.StoreConfig.WITHOUT_DUPLICATES_WITH_PREFIXING;
import static jetbrains.exodus.env.StoreConfig.WITH_DUPLICATES;

public class XodusResourceService implements ResourceService {

    private static final int LIST_BATCH_SIZE = 100;

    private static final Logger logger = LoggerFactory.getLogger(XodusResourceService.class);

    /**
     * The name of the {@link Store} for storing the persistent data associated with {@link Resource} entries.  Since
     * {@link Resource} instances may not be capable of storage this store may not be a complete collection of all
     * {@link Resource} instances in the system.
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
     * {@link ResourceService}.
     */
    public static final String STORE_PATHS_REVERSE = "paths_reverse";

    private Environment environment;

    private ResourceLoader resourceLoader;

    private ResourceLockService resourceLockService;

    private final AtomicReference<XodusCacheStorage> xodusCacheStorageAtomicReference = new AtomicReference<>(new XodusCacheStorage());

    @Override
    public Resource getAndAcquireResourceWithId(final ResourceId resourceId) {
        checkOpen();
        return getEnvironment().computeInReadonlyTransaction(txn -> doGetAndAcquireResource(txn, resourceId));
    }

    private XodusResource doGetAndAcquireResource(final Transaction txn, final ResourceId resourceId) {

        final ByteIterable key = stringToEntry(resourceId.asString());
        final XodusResource resource = doGetAndAcquireResource(txn, key);

        if (resource == null) {
            throw new ResourceNotFoundException("Resource not found: " + resourceId);
        }

        return resource;

    }

    private XodusResource doGetAndAcquireResource(final Transaction txn, final ByteIterable key) {

        final Store reverse = openReversePaths(txn);
        final XodusCacheStorage xodusCacheStorage = getStorage();

        // Check that there is at least one path pointing to the resource id.  If there is, then we can deal with
        // either fetching the resource from memory of from the persistent storage of resources on disk.

        if (reverse.get(txn, key) == null) {
            return null;
        }

        final Store resources = openResources(txn);
        final ByteIterable value = resources.get(txn, key);
        final XodusCacheKey xodusCacheKey = new XodusCacheKey(key);

        try (final ResourceLockService.Monitor m = getResourceLockService().getMonitor(xodusCacheKey.getResourceId())) {
            return xodusCacheStorage.getResourceIdResourceMap().computeIfAbsent(xodusCacheKey, k -> {

                final int length = value.getLength();
                final byte[] bytes = value.getBytesUnsafe();

                try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes, 0, length)) {
                    return new XodusResource(getResourceLoader().load(bis), getStorage());
                } catch (IOException ex) {
                    throw new InternalException(ex);
                }

            }).acquire();
        }

    }

    @Override
    public Resource getAndAcquireResourceAtPath(final Path path) {

        checkOpen();

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot fetch single resource with wildcard path " + path);
        }

        return getEnvironment().computeInReadonlyTransaction(txn -> {

            final Store store = openPaths(txn);
            final ByteIterable pathKey = stringToEntry(path.toNormalizedPathString());
            final ByteIterable resourceIdKey = store.get(txn, pathKey);

            if (resourceIdKey == null) {
                throw new ResourceNotFoundException("Resource at path not found: " + path);
            }

            final XodusResource xodusResource = doGetAndAcquireResource(txn, resourceIdKey);

            if (xodusResource == null) {
                throw new ResourceNotFoundException("Resource at path not found: " + path);
            }

            return xodusResource;

        });

    }

    @Override
    public void addAndReleaseResource(final Path path, final Resource resource) {

        checkOpen();

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        getEnvironment().executeInTransaction(txn -> {

            final Store paths = openPaths(txn);
            final Store resources = openResources(txn);
            final ByteIterable pathKey = stringToEntry(path.toNormalizedPathString());
            final ByteIterable resourceIdKey = stringToEntry(resource.getId().asString());
            final XodusResource xodusResource = new XodusResource(resource, getStorage()).acquire();

            doLink(txn, paths, resourceIdKey, pathKey);
            doReleaseResource(txn, resources, xodusResource);

        });

    }

    @Override
    public Resource addAndAcquireResource(final Path path, final Resource resource) {

        checkOpen();

        if (path.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final XodusResource xodusResource =  getEnvironment().computeInTransaction(txn -> {
            final Store paths = openPaths(txn);
            final ByteIterable pathKey = stringToEntry(path.toNormalizedPathString());
            final ByteIterable resourceIdKey = stringToEntry(resource.getId().asString());
            doLink(txn, paths, resourceIdKey, pathKey);
            return new XodusResource(resource, getStorage());
        });

        try (final ResourceLockService.Monitor m = getResourceLockService().getMonitor(xodusResource.getId())) {
            return xodusResource.acquire();
        }

    }

    @Override
    public void release(final Resource resource) {

        checkOpen();

        final XodusResource xodusResource = checkXodusResource(resource);

        getEnvironment().executeInTransaction(txn -> {

            final Store reverse = openReversePaths(txn);
            final ByteIterable key = xodusResource.getXodusCacheKey().getKey();

            if (reverse.get(txn, key) == null) {
                throw new ResourceNotFoundException("Resource not part of this ResourceService " + xodusResource.getId());
            }

            final Store resources = openResources(txn);
            doReleaseResource(txn, resources, xodusResource);

        });

    }

    private void doReleaseResource(final Transaction txn, final Store resources, final XodusResource xodusResource) {
        try (final ResourceLockService.Monitor m = getResourceLockService().getMonitor(xodusResource.getId())) {
            xodusResource.release(txn, resources);
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
        final ByteIterable destinationKey = stringToEntry(destination.toNormalizedPathString());

        getEnvironment().executeInTransaction(txn -> {

            final Store paths = openPaths(txn);
            final ByteIterable existing = paths.get(txn, destinationKey);

            if (existing != null) {
                final String existingResourceId = entryToString(resourceIdKey);
                throw new DuplicateException("Resource with id " + existingResourceId + " already exists at path " + destination);
            }

            doLink(txn, paths, resourceIdKey, destinationKey);

        });

    }

    private void doLink(final Transaction txn, final Store paths,
                        final ByteIterable resourceIdKey, final ByteIterable destinationKey) {
        final Store reverse = openReversePaths(txn);
        paths.put(txn, destinationKey, resourceIdKey);
        reverse.put(txn, resourceIdKey, destinationKey);
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
            try (final ResourceLockService.Monitor m = getResourceLockService().getMonitor(unlink.getResourceId())) {
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

        try (final ResourceLockService.Monitor m = getResourceLockService().getMonitor(resourceId)) {
            final XodusCacheKey cacheKey = new XodusCacheKey(resourceId);
            final XodusResource xodusResource = getStorage().getResourceIdResourceMap().remove(cacheKey);
            final Resource resource = xodusResource == null ? DeadResource.getInstance() : xodusResource.getDelegate();
            resource.close();
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

            return streams.stream().flatMap(xrs -> xrs).map(xr -> xr.getDelegate());

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

        getEnvironment().executeInExclusiveTransaction(txn -> {

            // This is very critical that closing tries to force everything

            final Store paths = openPaths(txn);
            final Store reverse = openReversePaths(txn);
            final Store resources = openResources(txn);

            streams.stream().flatMap(identity()).distinct().forEach(xr -> {

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

                try {
                    xr.close();
                } catch (Exception ex) {
                    logger.error("Caught exception closing Resource {}", xr.getId(), ex);
                }

            });

        });

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
