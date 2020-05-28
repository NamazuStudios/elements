package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.createLink;

public class UnixFSRevisionDataStore implements RevisionDataStore {

    public static final String STORAGE_ROOT_DIRECTORY = "com.namazustudios.socialengine.rt.transact.unix.fs.root";

    private final Path lockFilePath;

    private final UnixFSUtils utils;

    private final UnixFSPathIndex pathIndex;

    private final UnixFSResourceIndex resourceIdIndex;

    @Inject
    public UnixFSRevisionDataStore(
            final UnixFSUtils utils,
            final UnixFSPathIndex pathIndex,
            final UnixFSResourceIndex resourceIdIndex,
            @Named(STORAGE_ROOT_DIRECTORY) final Path storageRoot) throws IOException {
        this.utils = utils;
        this.pathIndex = pathIndex;
        this.resourceIdIndex = resourceIdIndex;
        this.lockFilePath = utils.lockPath(storageRoot);
    }

    @Override
    public void close() {
        this.utils.unlockDirectory(lockFilePath);
    }

    @Override
    public UnixFSPathIndex getPathIndex() {
        return pathIndex;
    }

    @Override
    public UnixFSResourceIndex getResourceIndex() {
        return resourceIdIndex;
    }

    public UnixFSTransactionProgramInterpreter.ExecutionHandler newExecutionHandler(final Revision<?> revision) {
        return new UnixFSTransactionProgramInterpreter.ExecutionHandler() {

            @Override
            public void unlinkFile(final java.nio.file.Path fsPath) {
                utils.doOperationV(() -> Files.delete(fsPath), FatalException::new);
            }

            @Override
            public void unlinkRTPath(final com.namazustudios.socialengine.rt.Path rtPath) {
                getPathIndex().unlink(revision, rtPath);
            }

            @Override
            public void removeResource(final ResourceId resourceId) {
                getResourceIndex().removeResource(revision, resourceId);
            }

            @Override
            public void linkFSPathToRTPath(final java.nio.file.Path fsPath,
                                           final com.namazustudios.socialengine.rt.Path rtPath) {
                getPathIndex().linkFSPathToRTPath(revision, rtPath, fsPath);
            }

            @Override
            public void linkFSPathToResourceId(final Path fsPath,
                                               final ResourceId resourceId) {
                getResourceIndex().linkFSPathToResourceId(revision, fsPath, resourceId);
            }

            @Override
            public void linkResourceToRTPath(final ResourceId resourceId,
                                             final com.namazustudios.socialengine.rt.Path rtPath) {

                final UnixFSPathIndex.PathMapping pathMapping = pathIndex.getPathMapping(rtPath);
                final UnixFSResourceIndex.PathMapping resourceMapping = resourceIdIndex.getPathMapping(resourceId);

                utils.doOperationV(() -> {
                    final Path rtPathPath = pathMapping.getRevisionPath(revision);
                    final Path resourcePath = resourceMapping.getRevisionPath(revision);
                    createLink(resourcePath, rtPathPath);
                }, FatalException::new);

            }

        };
    }

}
