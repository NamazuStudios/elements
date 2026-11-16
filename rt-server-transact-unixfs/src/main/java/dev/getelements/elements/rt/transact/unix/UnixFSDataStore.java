package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.transact.DataStore;

import jakarta.inject.Inject;
import java.nio.file.Files;
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
            try (final var nodeStream = newDirectoryStream(nodeDirectory);
                 final var garbageStream = newDirectoryStream(garbageDirectory)) {

                if (nodeStream instanceof SecureDirectoryStream<Path> node
                        && garbageStream instanceof SecureDirectoryStream<Path> garbage) {
                    node.move(nodeDirectory, garbage, nodeDirectory.getFileName());
                } else {
                    // Not every filesystem supplies a SecureDirectoryStream (macOS's NIO implementation
                    // never does); fall back to a plain move. The relative move above is atomic where
                    // supported; this variant is not, but the target is the garbage directory, whose
                    // contents are deleted either way.
                    Files.move(nodeDirectory, garbageDirectory.resolve(nodeDirectory.getFileName()));
                }

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
