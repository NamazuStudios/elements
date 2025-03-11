package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.transact.DataStore;

import jakarta.inject.Inject;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.lang.String.format;
import static java.nio.file.Files.newDirectoryStream;

public class UnixFSDataStore implements DataStore {

    private static final Executor reaper = Executors.newSingleThreadExecutor(r -> {
        final var thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName(format("%s reaper.", UnixFSDataStore.class.getName()));
        return thread;
    });

    private UnixFSTaskIndex taskIndex;

    private UnixFSPathIndex pathIndex;

    private UnixFSResourceIndex resourceIndex;

    private UnixFSUtils unixFSUtils;

    @Override
    public void removeAllResources(final NodeId nodeId) {

        final var nodeDirectory = getUnixFSUtils().resolveNodeStorageRoot(nodeId);
        final var garbageDirectory = getUnixFSUtils().allocateGarbageDirectory();

        getUnixFSUtils().doOperationV(() -> {
            try (final var node = (SecureDirectoryStream<Path>) newDirectoryStream(nodeDirectory);
                 final var garbage = (SecureDirectoryStream<Path>) newDirectoryStream(garbageDirectory)) {
                node.move(nodeDirectory, garbage, nodeDirectory.getFileName());
            }
        });

        reaper.execute(() -> getUnixFSUtils().cleanupGarbage(garbageDirectory));

    }

    @Override
    public UnixFSTaskIndex getTaskIndex() {
        return taskIndex;
    }

    @Inject
    public void setTaskIndex(UnixFSTaskIndex taskIndex) {
        this.taskIndex = taskIndex;
    }

    @Override
    public UnixFSPathIndex getPathIndex() {
        return pathIndex;
    }

    @Inject
    public void setPathIndex(UnixFSPathIndex pathIndex) {
        this.pathIndex = pathIndex;
    }

    @Override
    public UnixFSResourceIndex getResourceIndex() {
        return resourceIndex;
    }

    @Inject
    public void setResourceIndex(UnixFSResourceIndex resourceIndex) {
        this.resourceIndex = resourceIndex;
    }

    public UnixFSUtils getUnixFSUtils() {
        return unixFSUtils;
    }

    @Inject
    public void setUnixFSUtils(UnixFSUtils unixFSUtils) {
        this.unixFSUtils = unixFSUtils;
    }

}
