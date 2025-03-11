package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.LockSetService;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.util.Monitor;

import jakarta.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.cluster.path.Paths.WILDCARD_FIRST;
import static dev.getelements.elements.sdk.cluster.path.Paths.WILDCARD_LAST;
import static java.util.stream.Collectors.toCollection;

public class StandardSnapshotBuilder implements Snapshot.Builder {

    private DataStore dataStore;

    private LockSetService lockSetService;

    private List<Path> pathList = new LinkedList<>();

    private List<ResourceId> resourceIdList = new LinkedList<>();

    @Override
    public StandardSnapshotBuilder load(final Path path) {
        path.getOptionalNodeId().orElseThrow(() -> new IllegalArgumentException("Path must have NodeId"));
        pathList.add(path);
        return this;
    }

    @Override
    public StandardSnapshotBuilder load(final ResourceId resourceId) {
        resourceIdList.add(resourceId);
        return this;
    }

    @Override
    public Snapshot buildRO() {

        final var lockSetService = getLockSetService();

        return doBuild(
                lockSetService::getPathReadMonitor,
                lockSetService::getResourceIdReadMonitor
        );

    }
    @Override
    public Snapshot buildRW() {

        final var lockSetService = getLockSetService();

        return doBuild(
                lockSetService::getPathWriteMonitor,
                lockSetService::getResourceIdWriteMonitor
        );

    }

    public Snapshot doBuild(
            final Function<SortedSet<Path>, Monitor> pathMonitorSupplier,
            final Function<SortedSet<ResourceId>, Monitor> resourceIdMonitorSupplier) {

        var monitor = Monitor.empty();

        try {

            // Wa acquire all resource ids which will ensure that any paths owned by the resource IDs will also be
            // locked in a consistent manner.
            final var resourceIds = loadResourceIds();
            monitor = monitor.then(resourceIdMonitorSupplier.apply(resourceIds));

            // Given all resource IDs, we get paths to lock out in the final list of all paths.
            // final var loadedPaths = loadPaths(resourceIds);

            // We finally have the fully locked scope of all resources and paths which has been locked in an orderly
            // fashion.

            final var allPaths = pathList.stream()
                    .map(Path::toWildcardRecursive)
                    .collect(toCollection(() -> new TreeSet<>(WILDCARD_LAST)));

            monitor = monitor.then(pathMonitorSupplier.apply(allPaths));

            // The Snapshot can now be built with full scope of the information needed to perform the operation
            // against the data store.

            return new StandardSnapshot(monitor, getDataStore(), resourceIds, allPaths);

        } catch (Exception ex) {
            // This is an exception guard to ensure that a partially build monitor will always be released in case
            // there is any type of exception. This follows downstream into the other code.
            monitor.close();
            throw ex;
        }

    }

    private SortedSet<ResourceId> loadResourceIds() {

        final var pathIndex = getDataStore().getPathIndex();

        final var nonWildcardPaths = pathList
                .stream()
                .collect(toCollection(() -> new TreeSet<>(WILDCARD_FIRST)));

        return Stream.concat(
                        resourceIdList.stream(),
                        nonWildcardPaths.stream()
                                .map(pathIndex::findResourceId)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                )
                .collect(toCollection(TreeSet::new));

    }

//    private List<Path> loadPaths(final Set<ResourceId> resourceIds) {
//
//        final var resourceIndex = getDataStore().getResourceIndex();
//
//        return resourceIds
//                .stream()
//                .map(resourceIndex::findReversePaths)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .flatMap(Collection::stream)
//                .collect(Collectors.toList());
//
//    }

    public DataStore getDataStore() {
        return dataStore;
    }

    @Inject
    public void setDataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public LockSetService getLockSetService() {
        return lockSetService;
    }

    @Inject
    public void setLockSetService(LockSetService lockSetService) {
        this.lockSetService = lockSetService;
    }

}
