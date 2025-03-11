package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.FatalException;
import dev.getelements.elements.rt.transact.TaskEntry;
import dev.getelements.elements.rt.transact.TaskEntry.OperationalStrategy;
import dev.getelements.elements.rt.transact.TaskIndex;

import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.getelements.elements.rt.transact.unix.UnixFSUtils.*;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public class UnixFSTaskIndex implements TaskIndex {

    private UnixFSUtils unixFSUtils;

    @Override
    public Stream<Function<OperationalStrategy<ResourceId>, TaskEntry<ResourceId>>> listAllEntriesByResource() {
        return getUnixFSUtils().doOperation(() -> {

            final var nodeStorageRoot = getUnixFSUtils().getNodeStorageRoot();

            return !isDirectory(nodeStorageRoot, NOFOLLOW_LINKS)
                    ? Stream.empty()
                    : Files
                        .list(nodeStorageRoot)
                        .flatMap(nodeRoot -> {
                            try {
                                final var taskRoot = nodeRoot.resolve(TASKS_DIRECTORY);
                                return isDirectory(taskRoot)
                                        ? Files.list(taskRoot)
                                        : Stream.empty();
                            } catch (IOException e) {
                                throw new FatalException(e);
                            }
                        })
                        .filter(path -> getUnixFSUtils().isMatchingExtension(path, TASK_EXTENSION))
                        .map(this::open);

        });
    }

    public List<Path> allTasks() throws IOException{
        return Files
                .list(getUnixFSUtils().getNodeStorageRoot())
                .flatMap(nodeRoot -> {
                    try {
                        return Files.list(nodeRoot.resolve(NODE_DIRECTORY));
                    } catch (IOException e) {
                        throw new FatalException(e);
                    }
                })
                .filter(path -> getUnixFSUtils().isMatchingExtension(path, TASK_EXTENSION))
                .collect(Collectors.toList());
    }

    private Function<OperationalStrategy<ResourceId>, TaskEntry<ResourceId>> open(final Path path) {
        return strategy -> getUnixFSUtils().doOperation(() -> {
            final var mapping = UnixFSTaskPathMapping.fromFSPath(getUnixFSUtils(), path);
            return new UnixFSTaskEntryExisting(strategy, getUnixFSUtils(), mapping);
        });
    }

    @Override
    public Optional<TaskEntry<ResourceId>> findTaskEntry(Supplier<OperationalStrategy<ResourceId>> ctor, ResourceId resourceId) {
        final var mapping = UnixFSTaskPathMapping.fromResourceId(getUnixFSUtils(), resourceId);
        return getUnixFSUtils().doOperation(() -> isRegularFile(mapping.getFilesystemPath())
                ? Optional.of(new UnixFSTaskEntryExisting(ctor.get(), getUnixFSUtils(), mapping))
                : Optional.empty());
    }

    @Override
    public TaskEntry<ResourceId> getOrCreateTaskEntry(
            final Supplier<OperationalStrategy<ResourceId>> ctor,
            final ResourceId resourceId) {
        return getUnixFSUtils().doOperation(() -> new UnixFSTaskEntryNew(ctor.get(), getUnixFSUtils(), resourceId));
    }

    @Override
    public void cleanup(
            final ResourceId resourceId,
            final String transactionId) {
        final var mapping = UnixFSTaskPathMapping.fromResourceId(getUnixFSUtils(), resourceId);
        getUnixFSUtils().cleanup(mapping, transactionId);
    }

    @Override
    public void applyChange(
            final ResourceId resourceId,
            final String transactionId) {
        final var mapping = UnixFSTaskPathMapping.fromResourceId(getUnixFSUtils(), resourceId);
        getUnixFSUtils().commit(mapping, transactionId);
    }

    public UnixFSUtils getUnixFSUtils() {
        return unixFSUtils;
    }

    @Inject
    public void setUnixFSUtils(UnixFSUtils unixFSUtils) {
        this.unixFSUtils = unixFSUtils;
    }

}
