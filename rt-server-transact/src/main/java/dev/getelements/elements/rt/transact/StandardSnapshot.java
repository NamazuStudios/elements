package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.SimpleResourceServiceListing;
import dev.getelements.elements.rt.exception.DuplicateException;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.cluster.id.TaskId;
import dev.getelements.elements.sdk.util.FinallyAction;
import dev.getelements.elements.sdk.util.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.cluster.path.Paths.WILDCARD_FIRST;
import static dev.getelements.elements.sdk.cluster.path.Paths.WILDCARD_LAST;
import static dev.getelements.elements.rt.transact.NullResourceEntry.nullInstance;
import static java.util.Collections.*;

public class StandardSnapshot implements Snapshot {

    private static final Logger logger = LoggerFactory.getLogger(StandardSnapshot.class);

    private FinallyAction onClose;

    private final DataStore dataStore;

    private final SortedSet<Path> paths;

    private final SortedSet<ResourceId> resourceIds;

    private final SortedMap<ResourceId, TaskEntry<ResourceId>> taskEntryMap = new TreeMap<>();

    private final SortedMap<Path, ResourceEntry> pathEntryMap = new TreeMap<>(WILDCARD_FIRST);

    private final SortedMap<ResourceId, ResourceEntry> resourceIdEntryMap = new TreeMap<>();

    public StandardSnapshot(
            final Monitor monitor,
            final DataStore dataStore,
            final SortedSet<ResourceId> resourceIds,
            final SortedSet<Path> paths) {

        this.dataStore = dataStore;
        this.paths = unmodifiableSortedSet(paths);
        this.resourceIds = unmodifiableSortedSet(resourceIds);
        this.onClose = FinallyAction.begin(logger).then(monitor::close);

        if (!WILDCARD_LAST.equals(paths.comparator())) {
            throw new IllegalArgumentException("Paths must be sorted WILDCARD_LAST");
        }

    }

    @Override
    public Stream<ResourceService.Listing> list(final Path path) {

        check(path);

        final var snapshotStream = pathEntryMap
                .tailMap(path)
                .entrySet()
                .stream()
                .takeWhile(mapEntry -> path.matches(mapEntry.getKey()))
                .filter(mapEntry -> mapEntry.getValue().isPresent())
                .map(mapEntry -> {
                    final var result = new SimpleResourceServiceListing();
                    result.setPath(mapEntry.getKey());
                    result.setResourceId(mapEntry.getValue().getOriginalResourceId());
                    return result;
                });

        final var datastoreStream = getDataStore()
                .getPathIndex()
                .list(path)
                .filter(listing -> {
                    final var entry = pathEntryMap.get(listing.getPath());
                    return entry == null || entry.isPresent();
                })
                .map(listing -> {
                    try (var entry = listing.open()) {
                        final var result = new SimpleResourceServiceListing();
                        result.setPath(listing.getPath());
                        result.setResourceId(entry.getResourceId());
                        return result;
                    }
                });

        onClose = onClose
                .then(snapshotStream::close)
                .then(datastoreStream::close);

        return Stream.concat(snapshotStream, datastoreStream);

    }

    @Override
    public Optional<TaskEntry<ResourceId>> findTaskEntry(final ResourceId resourceId) {

        check(resourceId);

        final var taskEntry = taskEntryMap.computeIfAbsent(resourceId, r ->  {

            final var optional = getDataStore()
                    .getTaskIndex()
                    .findTaskEntry(InternalTaskOperationalStrategy::new, r);

            optional.ifPresent(entry -> onClose = onClose.then(entry::close));
            return optional.orElse(NullTaskEntry.nullInstance());

        });

        return taskEntry.isPresent() ? Optional.of(taskEntry) : Optional.empty();

    }

    @Override
    public TaskEntry<ResourceId> getOrCreateTaskEntry(final ResourceId resourceId) {

        check(resourceId);

        if (findResourceEntry(resourceId).isEmpty()) {
            throw new ResourceNotFoundException("No resource exists: " + resourceId);
        }

        return taskEntryMap.compute(resourceId, (key, existing) ->  {

            if ((existing == null) || (NullTaskEntry.isNull(existing))) {

                final var entry = getDataStore()
                        .getTaskIndex()
                        .getOrCreateTaskEntry(InternalTaskOperationalStrategy::new, resourceId);

                onClose = onClose.then(entry::close);
                return entry;

            } else {
                existing.delete();
                return existing;
            }

        });

    }

    @Override
    public Optional<ResourceEntry> findResourceEntry(final Path path) {

        check(path);

        final var entry = pathEntryMap.computeIfAbsent(path, _key -> {

            final var optional = getDataStore()
                    .getPathIndex()
                    .findEntry(path, InternalResourceOperationalStrategy::new);

            optional.ifPresent(e -> {

                final var resourceId = check(e).getOriginalResourceId();

                onClose = onClose.then(e::close);

                final var result = resourceIdEntryMap.put(resourceId, e);

                if (result != null && result.isPresent()) {
                    logger.warn("Detected duplicate mapping for {} -> {}", resourceId, path);
                }

            });

            return optional.orElse(nullInstance());

        });

        return entry.isPresent() ? Optional.of(entry) : Optional.empty();

    }

    @Override
    public Optional<ResourceEntry> findResourceEntry(final ResourceId resourceId) {

        check(resourceId);

        final var entry = resourceIdEntryMap.computeIfAbsent(resourceId, _r -> {

            final var optional =  getDataStore()
                    .getResourceIndex()
                    .findEntry(resourceId, InternalResourceOperationalStrategy::new);

            optional.ifPresent(e -> {

                onClose = onClose.then(e::close);

                e.getOriginalReversePathsImmutable().forEach(p -> {

                    final var existing = pathEntryMap.put(p, e);

                    if (existing != null && existing.isPresent()) {
                        logger.warn("Detected duplicate mapping for {} -> {}", resourceId, p);
                    }

                });

            });

            return optional.orElse(nullInstance());

        });

        return entry.equals(nullInstance()) ? Optional.empty() : Optional.of(entry);

    }

    @Override
    public Collection<TaskEntry<?>> getTaskEntries() {
        return unmodifiableCollection(taskEntryMap.values());
    }

    @Override
    public Collection<ResourceEntry> getResourceEntries() {
        return unmodifiableCollection(resourceIdEntryMap.values());
    }

    @Override
    public ResourceEntry add(final ResourceId resourceId) {

        check(resourceId);

        return resourceIdEntryMap.compute(resourceId, (rid, existing) -> {
            if (existing == null || existing.isAbsent()) {
                return getDataStore()
                        .getResourceIndex()
                        .newEntry(resourceId, InternalResourceOperationalStrategy::new);
            } else {
                throw new DuplicateException("Resource already exists: " + resourceId);
            }
        });

    }

    private ResourceEntry check(final ResourceEntry entry) {
        check(entry.getOriginalResourceId());
        return entry;
    }

    private ResourceId check(final ResourceId resourceId) {

        if (!resourceIds.contains(resourceId)) {
            throw new SnapshotMissException("Resource ID not in scope for this snapshot: " + resourceId);
        }

        return resourceId;

    }

    private Path check(final Path path) {
        if (paths.stream().anyMatch(path::matches)) {
            return path;
        } else {
            throw new SnapshotMissException("Path not in scope for this snapshot: " + path);
        }
    }

    @Override
    public void close() {
        onClose.close();
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    private class InternalTaskOperationalStrategy implements TaskEntry.OperationalStrategy<ResourceId> {

        private Map<TaskId, TransactionalTask> tasks;

        @Override
        public Optional<ResourceId> doFindScope(final TaskEntry<ResourceId> taskEntry) {
            return tasks == null || !tasks.isEmpty()
                    ? taskEntry.findOriginalScope()
                    : Optional.empty();
        }

        @Override
        public boolean doDelete(final TaskEntry<ResourceId> taskEntry) {
            if (tasks == null || tasks.isEmpty()) {
                return false;
            } else {
                tasks.clear();
                return true;
            }
        }

        @Override
        public Map<TaskId, TransactionalTask> doGetTasksImmutable(final TaskEntry<ResourceId> taskEntry) {
            return tasks == null
                    ? taskEntry.getOriginalTasksImmutable()
                    : unmodifiableMap(tasks);
        }

        @Override
        public boolean doAddTask(final TaskEntry<ResourceId> taskEntry,
                                 final TaskId taskId,
                                 final long timestamp) {

            check(taskId.getResourceId());

            final var result = doGetTasksMutable(taskEntry).put(taskId, new TransactionalTask() {
                @Override
                public TaskId getTaskId() {
                    return taskId;
                }

                @Override
                public long getTimestamp() {
                    return timestamp;
                }
            });

            return (result == null) || !(result.getTimestamp() == timestamp);

        }

        @Override
        public boolean doDeleteTask(final TaskEntry<ResourceId> taskEntry, final TaskId taskId) {
            check(taskId.getResourceId());
            return doGetTasksMutable(taskEntry).remove(taskId) != null;
        }

        private Map<TaskId, TransactionalTask> doGetTasksMutable(final TaskEntry<ResourceId> taskEntry) {
            return tasks == null
                    ? (tasks = new TreeMap<>(taskEntry.getOriginalTasksImmutable()))
                    : tasks;
        }

    }

    private class InternalResourceOperationalStrategy implements ResourceEntry.OperationalStrategy {

        private boolean deleted = false;

        private Set<Path> reverse;

        private ResourceContents resourceContents;

        @Override
        public Optional<ResourceId> doFindResourceId(final ResourceEntry entry) {
            return deleted
                    ? Optional.empty()
                    : entry.findOriginalResourceId();
        }

        @Override
        public boolean doLink(final ResourceEntry entry, final Path toLink) {

            check(toLink);

            if (entry.isAbsent()) {
                throw new IllegalStateException("Entry is absent.");
            }

            return pathEntryMap.compute(toLink, (path, existing) -> {
                if (existing == null || existing.isAbsent()) {

                    final var reverse = doGetReverseLinksMutable(entry);

                    if (!reverse.add(toLink)) {
                        logger.warn("Path already exists in reverse mapping: {}", toLink);
                        throw new InternalException("Path already exists in reverse mapping:" + toLink);
                    }

                    return entry;

                } else {
                    throw new DuplicateException("Resource exists at path: " + toLink);
                }
            }).isPresent();

        }

        @Override
        public boolean doUnlink(final ResourceEntry entry, final Path toUnlink) {

            check(toUnlink);

            if (entry.isAbsent()) {
                throw new IllegalStateException("Entry is absent.");
            }

            final var result = pathEntryMap.compute(toUnlink, (path, existing) -> {

                if (existing == null || existing.isAbsent()) {
                    throw new ResourceNotFoundException("No resource exists for path: " + toUnlink);
                }

                final var reverse = doGetReverseLinksMutable(entry);

                if (!reverse.remove(toUnlink)) {
                    throw new InternalException("Path does not exist in reverse mapping:" + toUnlink);
                }

                return entry;

            });

            return result.isAbsent();

        }

        @Override
        public boolean doDelete(final ResourceEntry entry) {
            if (deleted) {
                return false;
            } else if (reverse == null) {
                deleted = true;
                return true;
            } else {
                deleted = true;
                return true;
            }
        }

        @Override
        public ResourceContents doUpdateResourceContents(final ResourceEntry entry) {
            return resourceContents = getDataStore().getResourceIndex().updateContents(entry.getResourceId());
        }

        @Override
        public Set<Path> doGetReversePathsImmutable(final ResourceEntry entry) {
            final var result = doGetReverseLinksMutable(entry);
            return unmodifiableSet(result);
        }

        private Set<Path> doGetReverseLinksMutable(final ResourceEntry entry) {

            check(entry);

            if (reverse == null) {
                final var original = entry.getOriginalReversePathsImmutable();
                reverse = new TreeSet<>(WILDCARD_FIRST);
                reverse.addAll(original);
            }

            return reverse;

        }

        @Override
        public Optional<ResourceContents> doFindResourceContents(ResourceEntry entry) {
            return resourceContents == null ? entry.findOriginalResourceContents() : Optional.of(resourceContents);
        }

        @Override
        public boolean doIsOriginalContent(final ResourceEntry entry) {
            return !deleted && resourceContents == null;
        }

        @Override
        public boolean doIsOriginalReversePaths(ResourceEntry entry) {
            return reverse == null;
        }

    }

}
