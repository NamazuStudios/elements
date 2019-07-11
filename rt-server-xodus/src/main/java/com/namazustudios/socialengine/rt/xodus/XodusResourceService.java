package com.namazustudios.socialengine.rt.xodus;

import com.google.common.base.Stopwatch;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.ResourceLockService.Monitor;
import com.namazustudios.socialengine.rt.exception.DuplicateException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Cursor;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.Integer.max;
import static java.lang.System.getProperty;
import static java.util.Collections.unmodifiableSet;
import static java.util.Spliterator.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static jetbrains.exodus.bindings.IntegerBinding.entryToInt;
import static jetbrains.exodus.bindings.IntegerBinding.intToEntry;
import static jetbrains.exodus.bindings.StringBinding.entryToString;
import static jetbrains.exodus.bindings.StringBinding.stringToEntry;
import static jetbrains.exodus.env.StoreConfig.*;

public class XodusResourceService implements ResourceService {

    public static final String RESOURCE_ENVIRONMENT = "com.namazustudios.socialengine.rt.xodus.resource.environment";

    private static final Logger logger = LoggerFactory.getLogger(XodusResourceService.class);

    public static final String VERBOSE_LOGGER_NAME = XodusResourceService.class.getName() + ".verbose";

    /**
     * This logger an extemely verbose logger which will cause several full-database dumps for every operation.  Useful
     * only for debugging and unit testing.  Setting this logger to trace will severely impact performance and may cause
     * {@link OutOfMemoryError}s
     */
    private static final Logger verboseLogger = LoggerFactory.getLogger(VERBOSE_LOGGER_NAME);

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
     * The name of the {@link Store} which links {@link ResourceId} instances to many {@link Path} instances and
     * represents a comprehensive list of all {@link ResourceId}s tracked by this {@link ResourceService}.
     *
     * The literal value of this constant is there to preserve compatibility with existing databases.
     */
    public static final String STORE_RESOURCE_IDS = "paths_reverse";

    /**
     * The name of the {@link Store} which counts the number of acquires for each resource.  This contains a key to
     * number mapping.  This only stores values if the resource is acquired.  If not acquires exist, the acquire count
     * is removed entirely.
     */
    public static final String STORE_ACQUIRES = "acquires";

    public static final Set<String> TEXT_STORES;

    public static final Set<String> BINARY_STORES;

    public static final Set<String> INTEGER_STORES;

    static {

        final Set<String> binaryStores = new HashSet<>();
        binaryStores.add(STORE_RESOURCES);
        BINARY_STORES = unmodifiableSet(binaryStores);

        final Set<String> integerStores = new HashSet<>();
        integerStores.add(STORE_ACQUIRES);
        INTEGER_STORES = unmodifiableSet(integerStores);

        final Set<String> textStores = new HashSet<>();
        textStores.add(STORE_PATHS);
        textStores.add(STORE_RESOURCE_IDS);
        TEXT_STORES = unmodifiableSet(textStores);

    }

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

    private final BiConsumer<Transaction, ByteIterable> debugPreRemove =
        !verboseLogger.isTraceEnabled() ? (t, r) -> {} :
        (txn, resourceIdKey) -> {

            final StringBuilder report = new StringBuilder();
            final ResourceId resourceId = new ResourceId(entryToString(resourceIdKey));

            report.append('\n').append("Removing Resource: ").append(resourceId.asString()).append("\n\n");
            dumpStoreData(report);
            verboseLogger.trace("{}", report);

        };

    private final BiConsumer<Transaction, ByteIterable> debugPostRemove =
        !verboseLogger.isTraceEnabled() ? (t, r) -> {} :
        (txn, resourceIdKey) -> {

            final StringBuilder report = new StringBuilder();
            final ResourceId resourceId = new ResourceId(entryToString(resourceIdKey));

            report.append('\n').append("Removed Resource: ").append(resourceId.asString()).append("\n\n");
            dumpStoreData(report);
            verboseLogger.trace("{}", report);

        };

    private final BiConsumer<Transaction, ByteIterable> debugPreUnlink =
        !verboseLogger.isTraceEnabled() ? (t, p) -> {} :
        (txn, pathKey) -> {

            final StringBuilder report = new StringBuilder();
            final Path path = Path.fromPathString(entryToString(pathKey));

            report.append('\n')
                  .append("Unlinking Path: ").append(path.toNormalizedPathString()).append("\n\n");
            dumpStoreData(report);
            verboseLogger.trace("{}", report);

        };

    private final UnlinkLogger debugPostUnlink =
        !verboseLogger.isTraceEnabled() ? (t, p, u) -> {} :
        (txn, pathKey, removed) -> {

            final StringBuilder report = new StringBuilder();
            final Path path = Path.fromPathString(entryToString(pathKey));

            report.append('\n')
                  .append("Unlinked Path: ").append(path.toNormalizedPathString())
                  .append(" Removed: ").append(removed).append("\n\n");

            dumpStoreData(report);
            verboseLogger.trace("{}", report);

        };

    private final ListLogger debugList =
        !verboseLogger.isTraceEnabled() ? (t, p, r) -> {} :
        (txn, path, result) -> {
            final StringBuilder report = new StringBuilder();
            report.append('\n')
                  .append("Listing Path: ").append(path.toNormalizedPathString()).append('\n');
            report.append("Found ").append(result.size()).append(" listings.").append('\n');
            result.forEach(listing -> report.append("Listing: ")
                                            .append(listing.getPath().toNormalizedPathString())
                                            .append(" -> ")
                                            .append(listing.getResourceId().asString())
                                            .append('\n'));
            dumpStoreData(report);
            verboseLogger.trace("{}", report);
        };

    @Override
    public void start() {
        getEnvironment().executeInExclusiveTransaction(txn -> {

            final Store acquires = openAcquires(txn);

            try (final Cursor cursor = acquires.openCursor(txn)) {

                int failures = 0;
                int existing = 0;

                while (cursor.getNext()) {
                    ++existing;
                    if (!cursor.deleteCurrent()) ++failures;
                }

                if (existing > 0) {
                    logger.warn("Opened storage with a count of {} acquired Resources {} of which failed to clear.  " +
                                "Possible storage corruption due to unclean shutdown.", existing, failures);
                }

            }

        });
    }

    @Override
    public boolean exists(final ResourceId resourceId) {
        final ByteIterable resourceIdKey = stringToEntry(resourceId.asString());
        return getEnvironment().computeInReadonlyTransaction(txn -> {
            final Store resourceIds = openResourceIds(txn);
            return resourceIds.get(txn, resourceIdKey) != null;
        });
    }

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

        final Store resourceIds = openResourceIds(txn);

        // Check that there is at least one path pointing to the resource id.  If there is, then we can deal with
        // either fetching the resource from memory of from the persistent storage of resources on disk.

        if (resourceIds.get(txn, resourceIdKey) == null) {
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
        final ByteIterable value = resources.get(txn, resourceIdKey);

        // We return a supplier, because we wish to avoid repeat attempts to use the serializer to actually read it from
        // a blob of bytes.  We also want to avoid acquiring a lock during the transactional phase of the method so
        // returning a supplier will side-step that issue while allowining us to handle edge cases when contention among
        // the same resource may be high.

        if (acquires == 1) {

            // Only read the bytes if we absolutely need to.

            if (value == null) {
                // This should never happen if the state of the database is consistent.
                throw new InternalException("Inconsistent state.  Newly acquired resource has no value.");
            }

            return () -> getOrLoad(xodusCacheKey, value);

        } else {
            return () -> getOrLoad(xodusCacheKey, value);
        }

    }

    private XodusResource readFromCache(final XodusCacheKey xodusCacheKey) {
        try (final Monitor m = getResourceLockService().getMonitor(xodusCacheKey.getResourceId())) {
            final XodusResource xr = getStorage().getResourceIdResourceMap().get(xodusCacheKey);
            if (xr == null) throw new InternalException("Resource not present in cache: " + xodusCacheKey.getResourceId());
            return xr;
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

    private XodusResource getOrLoad(final XodusCacheKey xodusCacheKey, final ByteIterable value) {
        try (final Monitor monitor = getResourceLockService().getMonitor(xodusCacheKey.getResourceId())) {

            final int length = value.getLength();
            final byte[] bytes = value.getBytesUnsafe();

            final Map<XodusCacheKey, XodusResource> cache = getStorage().getResourceIdResourceMap();

            XodusResource xodusResource = cache.get(xodusCacheKey);

            if (xodusResource == null) {
                try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes, 0, length)) {

                    xodusResource = new XodusResource(getResourceLoader().load(bis));

                    if (getStorage().getResourceIdResourceMap().put(xodusCacheKey, xodusResource) != null) {
                        logger.error("Consistency error.  Expecting no existing resource in cache.");
                    }

                } catch (IOException ex) {
                    throw new InternalException(ex);
                }
            }

            return xodusResource;

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

        }).getAsBoolean();

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
            if (acquires != 1) throw new IllegalStateException("Expecting newly acquired resource count of 1.  Got: " + acquires);

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
    public boolean tryRelease(final Resource resource) {

        checkOpen();

        final XodusResource xodusResource = checkXodusResource(resource);

        return getEnvironment().computeInTransaction(txn -> {
            final Store resourceIds = openResourceIds(txn);
            final ByteIterable key = xodusResource.getXodusCacheKey().getKey();
            return resourceIds.get(txn, key) == null ? (BooleanSupplier) () -> false : doReleaseResource(txn, xodusResource);
        }).getAsBoolean();

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

    private BooleanSupplier doReleaseResource(final Transaction txn,
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
                    }

                    try {
                        // Ensure that it's closed once it's persisted to avoid memory leaks.
                        xodusResource.unload();
                    } catch (Exception ex) {
                        logger.error("Could not unload resource.", ex);
                    }

                }

                return true;

            };

        } else {
            return () -> {
                // If possible, we may want to do some signaling through a condition variable?
                logger.trace("Nothing to be done releasing Resource.");
                return true;
            };
        }

    }

    @Override
    public Spliterator<Listing> list(final Path path) {

        checkOpen();

        final List<XodusListing> result = new ArrayList<>();

        getEnvironment().executeInTransaction(txn -> {

            final Store store = openPaths(txn);

            try (final Cursor cursor = store.openCursor(txn)) {

                ByteIterable key = stringToEntry(path.stripWildcard().toNormalizedPathString());
                ByteIterable value = cursor.getSearchKeyRange(key);

                while (value != null) {

                    key = cursor.getKey();
                    value = cursor.getValue();

                    final XodusListing xodusListing = new XodusListing(key, value);

                    if (path.matches(xodusListing.getPath())) {
                        result.add(xodusListing);
                    } else {
                        break;
                    }

                    if (!cursor.getNext()) {
                        break;
                    }

                }

            }

            debugList.report(txn, path, result);

        });

        return Spliterators.spliterator(result, CONCURRENT | IMMUTABLE | NONNULL);

    }

    @Override
    public void linkPath(final Path source, final Path destination) {

        if (destination.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final ByteIterable destinationPathKey = stringToEntry(destination.toNormalizedPathString());

        getEnvironment().executeInTransaction(txn -> {

            final Store paths = openPaths(txn);

            final ByteIterable sourcePathKey = stringToEntry(source.toNormalizedPathString());
            final ByteIterable resourceIdKey = paths.get(txn, sourcePathKey);

            if (resourceIdKey == null) {
                throw new ResourceNotFoundException("No resource at path: " + source);
            }

            final ByteIterable existing = paths.get(txn, destinationPathKey);

            if (existing != null) {
                final String existingResourceId = entryToString(resourceIdKey);
                throw new DuplicateException("Resource with id " + existingResourceId + " already exists at path " + destination);
            }

            doLink(txn, paths, resourceIdKey, destinationPathKey);

        });

    }

    @Override
    public void link(final ResourceId sourceResourceId, final Path destination) {

        checkOpen();

        if (destination.isWildcard()) {
            throw new IllegalArgumentException("Cannot add resources with wildcard path.");
        }

        final ByteIterable resourceIdKey = stringToEntry(sourceResourceId.asString());
        final ByteIterable destinationPathKey = stringToEntry(destination.toNormalizedPathString());

        getEnvironment().executeInTransaction(txn -> {

            final Store paths = openPaths(txn);
            final ByteIterable existing = paths.get(txn, destinationPathKey);

                if (existing != null) {
                    final String existingResourceId = entryToString(resourceIdKey);
                    throw new DuplicateException("Resource with id " + existingResourceId + " already exists at path " + destination);
                }

            doLink(txn, paths, resourceIdKey, destinationPathKey);

        });

    }

    private void doLink(final Transaction txn, final Store paths,
                        final ByteIterable resourceIdKey, final ByteIterable pathKey) {

        if (paths.get(txn, pathKey) != null) {
            throw new DuplicateException("Resources already exists at path {}" + entryToString(pathKey));
        }

        final Store resourceIds = openResourceIds(txn);
        paths.put(txn, pathKey, resourceIdKey);
        resourceIds.put(txn, resourceIdKey, pathKey);

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
        final Unlink unlink = getEnvironment().computeInTransaction(txn -> doUnlink(txn, pathKey));

        if (unlink.isRemoved()) {
            try (final Monitor m = getResourceLockService().getMonitor(unlink.getResourceId())) {
                final XodusCacheStorage xodusCacheStorage = getStorage();
                final XodusCacheKey cacheKey = new XodusCacheKey(unlink.getResourceId());
                final XodusResource xodusResource = xodusCacheStorage.getResourceIdResourceMap().remove(cacheKey);
                reovedResourceConsumer.accept(xodusResource == null ? DeadResource.getInstance() : xodusResource.getDelegate());
            } finally {
                getResourceLockService().delete(unlink.getResourceId());
            }
        }

        return unlink;

    }

    private Unlink doUnlink(final Transaction txn, final ByteIterable pathKey) {

        final Store paths = openPaths(txn);
        final Store resourceIds = openResourceIds(txn);
        final Store resources = openResources(txn);
        final Store acquires = openAcquires(txn);

        final ByteIterable resourceIdValue;

        debugPreUnlink.accept(txn, pathKey);

        try (final Cursor cursor = paths.openCursor(txn)) {

            // Lookup and remove from Paths if it exists.  If it doesn't exist, then simply throw the appropriate
            // ResourceNotFoundException.  The ResourceId will be used elsewhere and removed only if no more paths
            // exist for the key.

            resourceIdValue = cursor.getSearchKey(pathKey);

            if (resourceIdValue == null) {
                final Path path = Path.fromPathString(entryToString(pathKey));
                throw new ResourceNotFoundException("No resource at path " + path.toNormalizedPathString());
            } else if (cursor.deleteCurrent()) {
                if (logger.isTraceEnabled()) {
                    final Path path = Path.fromPathString(entryToString(pathKey));
                    final ResourceId resourceId = new ResourceId(entryToString(resourceIdValue));
                    logger.trace("Unlinked Path Entry {} -> {}", path.toNormalizedPathString(), resourceId);
                }
            } else {
                final String resourceId = entryToString(resourceIdValue);
                final Path path = Path.fromPathString(entryToString(pathKey));
                logger.error("Consistency error.  Unable to unlink path {} -> {}", path, resourceId);
            }

        }

        final boolean remove;

        try (final Cursor cursor = resourceIds.openCursor(txn)) {

            // Preemptively count the number of remaining entries and determine if this will be a future removal
            // operation.

            cursor.getSearchKey(resourceIdValue);
            remove = cursor.count() == 1;

            // Second, remove the object from the store by deleting the current entry in the cursor.
            if (cursor.getSearchBoth(resourceIdValue, pathKey) && cursor.deleteCurrent()) {
                if (logger.isTraceEnabled()) {
                    final String resourceId = entryToString(resourceIdValue);
                    final Path path = Path.fromPathString(entryToString(pathKey));
                    logger.trace("Successfully unlinked {} from {} ", path.toNormalizedPathString(), resourceId);
                }
            } else {
                final String resourceId = entryToString(resourceIdValue);
                final Path path = Path.fromPathString(entryToString(pathKey));
                logger.error("Consistency error.  Reverse mapping broken {} -> {}", path, resourceId);
            }

        }

        if (remove) {
            // Finally, do a hard delete if we're getting rid of the data completely.
            acquires.delete(txn, resourceIdValue);
            resources.delete(txn, resourceIdValue);
        }

        final ResourceId resourceId = new ResourceId(entryToString(resourceIdValue));

        debugPostUnlink.report(txn, pathKey, remove);

        return new Unlink() {
            @Override
            public ResourceId getResourceId() {
                return resourceId;
            }

            @Override
            public boolean isRemoved() {
                return remove;
            }
        };

    }

    @Override
    public Resource removeResource(final ResourceId resourceId) {

        checkOpen();

        final ByteIterable resourceIdKey = stringToEntry(resourceId.asString());

        try (final Monitor m = getResourceLockService().getMonitor(resourceId)) {
            final XodusCacheKey cacheKey = new XodusCacheKey(resourceId);
            final XodusResource xodusResource = getStorage().getResourceIdResourceMap().remove(cacheKey);
            final Resource resource = xodusResource == null ? DeadResource.getInstance() : xodusResource.getDelegate();
            getEnvironment().executeInTransaction(txn -> doRemoveResource(txn, resourceIdKey));
            return resource;
        } finally {
            getResourceLockService().delete(resourceId);
        }

    }

    private void doRemoveResource(final Transaction txn, final ByteIterable resourceIdKey) {
        final Store paths = openPaths(txn);
        final Store resourceIds = openResourceIds(txn);
        final Store resources = openResources(txn);
        final Store acquires = openAcquires(txn);
        doRemoveResource(txn, resourceIdKey, paths, resourceIds, resources, acquires);
    }

    private void doRemoveResource(final Transaction txn,
                                  final ByteIterable resourceIdKey,
                                  final Store paths,
                                  final Store resourceIds,
                                  final Store resources,
                                  final Store acquires) {

        debugPreRemove.accept(txn, resourceIdKey);

        try (final Cursor cursor = resourceIds.openCursor(txn)) {

            ByteIterable pathKey = cursor.getSearchKey(resourceIdKey);

            if (pathKey == null) {
                final String resourceId = entryToString(resourceIdKey);
                throw new ResourceNotFoundException("No resource with id " + resourceId);
            }

            do {

                if (logger.isTraceEnabled()) {
                    final Path path = Path.fromPathString(entryToString(pathKey));
                    logger.trace("Removing path {}", path.toNormalizedPathString());
                }

                if (!paths.delete(txn, pathKey)) {
                    final String path = entryToString(pathKey);
                    final String resourceId = entryToString(resourceIdKey);
                    logger.error("Consistency error.  Path mapping broken {} -> {}", path, resourceId);
                }

            } while (cursor.getNextDup() && (pathKey = cursor.getValue()) != null);

        }

        if (!resourceIds.delete(txn, resourceIdKey)) {
            final String resourceId = entryToString(resourceIdKey);
            logger.error("Consistency error.  Reverse mapping broken for {}.  Zero paths deleted.", resourceId);
        }

        acquires.delete(txn, resourceIdKey);
        resources.delete(txn, resourceIdKey);

        debugPostRemove.accept(txn, resourceIdKey);

    }

    @Override
    public Stream<Resource> removeAllResources() {

        checkOpen();

        return getEnvironment().computeInExclusiveTransaction(txn -> {
            getEnvironment().truncateStore(STORE_RESOURCES, txn);
            getEnvironment().truncateStore(STORE_PATHS, txn);
            getEnvironment().truncateStore(STORE_RESOURCE_IDS, txn);

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

        xodusCacheStorageAtomicReference.getAndAccumulate(null, (e, u) -> {
            streams.add(e.getResourceIdResourceMap().values().stream());
            return u;
        });

        getEnvironment().computeInExclusiveTransaction(txn -> {

            // This is very critical that closing tries to force everything

            final Store paths = openPaths(txn);
            final Store resourceIds = openResourceIds(txn);
            final Store resources = openResources(txn);
            final Store acquires = openAcquires(txn);

            final List<XodusResource> toClose = streams.stream().flatMap(identity()).distinct().map(xr -> {

                try {
                    logger.debug("Persisting {}", xr.getId());
                    xr.persist(txn, resources);
                } catch (Exception ex) {
                    try {
                        logger.error("Caught exception persisting resource {}  Destroying..", xr.getId(), ex);
                        doRemoveResource(txn, xr.getXodusCacheKey().getKey(), paths, resourceIds, resources, acquires);
                    } catch (Exception _ex) {
                        logger.error("Caught exception destroying resource {}.", xr.getId(), _ex);
                    }
                }

                return xr;

            }).collect(toList());

            try (final Cursor cursor = acquires.openCursor(txn)) {
                int failed = 0;
                while (cursor.getNext()) if (!cursor.deleteCurrent()) ++failed;
                if (failed > 0) logger.error("Failed to delete {} acquires.");
            }

            return toClose;

        }).forEach(xr -> {
            try {
                xr.unload();
            } catch (Exception ex) {
                logger.error("Caught exception unloading Resource {}", xr.getId(), ex);
            }
        });

        getEnvironment().close();

    }

    public void persist(final ResourceId resourceId) {

        final XodusCacheKey xodusCacheKey = new XodusCacheKey(resourceId);

        try (final Monitor monitor = getResourceLockService().getMonitor(resourceId)) {
            getEnvironment().executeInTransaction(txn -> {

                final Store acquiresStore = openAcquires(txn);
                final Store resourcesStore = openResources(txn);
                final ByteIterable acquiresByteIterable = acquiresStore.get(txn, xodusCacheKey.getKey());
                final int acquires = entryToInt(acquiresByteIterable);

                if (acquires < 1) {
                    logger.warn("Resource with ID '{}' not acquired.  No persistence necessary.", resourceId);
                    return;
                }

                final XodusResource xodusResource = readFromCache(xodusCacheKey);
                xodusResource.persist(txn, resourcesStore);

            });
        }

    }

    private Store openResources(final Transaction txn) {
        return getEnvironment().openStore(STORE_RESOURCES, WITHOUT_DUPLICATES, txn);
    }

    private Store openPaths(final Transaction txn) {
        return getEnvironment().openStore(STORE_PATHS, WITHOUT_DUPLICATES_WITH_PREFIXING, txn);
    }

    private Store openResourceIds(final Transaction txn) {
        return getEnvironment().openStore(STORE_RESOURCE_IDS, WITH_DUPLICATES, txn);
    }

    private Store openAcquires(final Transaction txn) {
        return getEnvironment().openStore(STORE_ACQUIRES, WITHOUT_DUPLICATES, txn);
    }

    /**
     * Accepting a {@link StringBuilder}, this will completely dump the store contents to the builder.  Useful and
     * strongly only recommended for debugging and testing purposes, this can potentially use enough memory to
     * cause an {@link OutOfMemoryError}.  Use it wisely.
     *
     * @param stringBuilder the output {@link StringBuilder}
     * @return the {@link StringBuilder} that was supplied to the method.
     */
    public StringBuilder dumpStoreData(final StringBuilder stringBuilder) {

        return getEnvironment().computeInReadonlyTransaction(txn -> {

            final List<String> stores = getEnvironment().getAllStoreNames(txn);

            stores.stream().filter(BINARY_STORES::contains).forEach(storeName -> {

                final Store store = getEnvironment().openStore(storeName, USE_EXISTING, txn);

                stringBuilder.append("Binary Store: ").append(store.getName()).append('\n')
                             .append("Configuration: ").append(store.getConfig()).append('\n');

                int count = 0;

                try (final Cursor cursor  = store.openCursor(txn)) {
                    while (cursor.getNext()) {
                        final String key = entryToString(cursor.getKey());
                        stringBuilder.append("Record # ").append(count++).append(": ")
                                     .append(key).append(" -> ").append("<binary>").append('\n');
                    }
                }

            });

            stringBuilder.append('\n');

            stores.stream().filter(INTEGER_STORES::contains).forEach(storeName -> {

                final Store store = getEnvironment().openStore(storeName, USE_EXISTING, txn);

                stringBuilder.append("Integer Store: ").append(store.getName()).append('\n')
                             .append("Configuration: ").append(store.getConfig()).append('\n');

                int count = 0;

                try (final Cursor cursor  = store.openCursor(txn)) {
                    while (cursor.getNext()) {
                        final String key = entryToString(cursor.getKey());
                        final Integer value = entryToInt(cursor.getValue());
                        stringBuilder.append("Record # ").append(count++).append(": ")
                                     .append(key).append(" -> ").append(value).append('\n');
                    }
                }

            });

            stringBuilder.append('\n');

            stores.stream().filter(TEXT_STORES::contains).forEach(storeName -> {

                final Store store = getEnvironment().openStore(storeName, USE_EXISTING, txn);

                stringBuilder.append("Text Store: ").append(store.getName()).append('\n')
                             .append("Configuration: ").append(store.getConfig()).append('\n');

                int count = 0;

                try (final Cursor cursor  = store.openCursor(txn)) {
                    while (cursor.getNext()) {
                        final String key = entryToString(cursor.getKey());
                        final String value = entryToString(cursor.getValue());
                        stringBuilder.append("Record # ").append(count++).append(": ")
                                     .append(key).append(" -> ").append(value).append('\n');
                    }
                }

            });

            stringBuilder.append('\n');

            return stringBuilder;

        });
    }

    public Environment getEnvironment() {
        return environment;
    }

    @Inject
    public void setEnvironment(@Named(XodusResourceService.RESOURCE_ENVIRONMENT) Environment environment) {
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
            throw new IllegalArgumentException("Not an Xodus managed Resource.", ex);
        }
    }

    @FunctionalInterface
    private interface ListLogger {  void report(Transaction txn, Path path, Collection<XodusListing> listings); }

    @FunctionalInterface
    private interface UnlinkLogger {  void report(Transaction txn, ByteIterable pathKey, boolean removed); }

}
