package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSPathMapping.fromPath;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSResourceIdMapping.fromResourceId;
import static java.nio.file.Files.createLink;

public class UnixFSRevisionDataStore implements RevisionDataStore {

    public static final String STORAGE_ROOT_DIRECTORY = "com.namazustudios.socialengine.rt.transact.unix.fs.root";

    private final UnixFSUtils utils;

    private final UnixFSPathIndex pathIndex;

    private final UnixFSResourceIndex resourceIdIndex;

    private final UnixFSGarbageCollector garbageCollector;

    @Inject
    public UnixFSRevisionDataStore(
            final UnixFSUtils utils,
            final UnixFSPathIndex pathIndex,
            final UnixFSResourceIndex resourceIdIndex,
            final UnixFSGarbageCollector garbageCollector) throws IOException {

        this.utils = utils;
        this.pathIndex = pathIndex;
        this.resourceIdIndex = resourceIdIndex;
        this.garbageCollector = garbageCollector;

        utils.initialize();
        utils.lockStorageRoot();

    }

    @Override
    public void close() {
        utils.unlockStorageRoot();
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
            public void unlinkFile(final UnixFSTransactionProgram program, final Path fsPath) {
                utils.doOperationV(() -> Files.delete(fsPath), FatalException::new);
            }

            @Override
            public void unlinkRTPath(final UnixFSTransactionProgram program,
                                     final com.namazustudios.socialengine.rt.Path rtPath) {
                getPathIndex().unlink(revision, rtPath);
            }

            @Override
            public void removeResource(final UnixFSTransactionProgram program, final ResourceId resourceId) {
                getResourceIndex().removeResource(revision, resourceId);
            }

            @Override
            public void linkFSPathToRTPath(final UnixFSTransactionProgram program, final Path fsPath,
                                           final com.namazustudios.socialengine.rt.Path rtPath) {
                getPathIndex().linkFSPathToRTPath(revision, rtPath, fsPath);
            }

            @Override
            public void linkFSPathToResourceId(final UnixFSTransactionProgram program, final Path fsPath,
                                               final ResourceId resourceId) {
                getResourceIndex().linkFSPathToResourceId(revision, fsPath, resourceId);
            }

            @Override
            public void linkResourceToRTPath(final UnixFSTransactionProgram program,
                                             final ResourceId resourceId,
                                             final com.namazustudios.socialengine.rt.Path rtPath) {

                // Map all the FS paths to RT paths.
                final NodeId nodeId = program.header.nodeId.get();
                final UnixFSPathMapping pathMapping = fromPath(utils, nodeId, rtPath);
                final UnixFSResourceIdMapping resourceIdMapping = fromResourceId(utils, resourceId);

                // Pin so we ensure that the latest version syncs up
                final Path rtPathPath = garbageCollector.pinLatest(pathMapping.getPathDirectory(), revision);
                final Path resourcePath = garbageCollector.pinLatest(resourceIdMapping.getResourceIdDirectory(), revision);
                utils.doOperationV(() -> createLink(resourcePath, rtPathPath), FatalException::new);

            }

        };
    }

}
