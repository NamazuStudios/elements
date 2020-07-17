package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.*;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSCircularBlockBuffer.Slice;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSRevisionTableEntry.State.COMMITTED;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSRevisionTableEntry.State.WRITING;
import static java.lang.String.format;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.StandardOpenOption.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for tracking the current revision and providing access to the various indices that access the underlying
 * data. This implements the current revision using a table of revisions stored in a {@link UnixFSCircularBlockBuffer}
 * whereby pending operations are
 */
public class UnixFSRevisionDataStore implements RevisionDataStore {

    private static final Logger logger = getLogger(UnixFSRevisionDataStore.class);

    private final UnixFSUtils utils;

    private final UnixFSPathIndex pathIndex;

    private final UnixFSResourceIndex resourceIdIndex;

    private final UnixFSRevisionPool revisionPool;

    private final UnixFSGarbageCollector garbageCollector;

    private final UnixFSChecksumAlgorithm preferredChecksum;

    private final UnixFSRevisionTable revisionTable;

    @Inject
    public UnixFSRevisionDataStore(
            final UnixFSUtils utils,
            final UnixFSPathIndex pathIndex,
            final UnixFSResourceIndex resourceIdIndex,
            final UnixFSRevisionPool revisionPool,
            final UnixFSGarbageCollector garbageCollector,
            final UnixFSChecksumAlgorithm preferredChecksum,
            final UnixFSRevisionTable revisionTable) throws IOException {

        this.utils = utils;
        this.pathIndex = pathIndex;
        this.resourceIdIndex = resourceIdIndex;
        this.revisionPool = revisionPool;
        this.garbageCollector = garbageCollector;
        this.preferredChecksum = preferredChecksum;
        this.revisionTable = revisionTable;

    }

    @Override
    public void close() {}

    @Override
    public UnixFSPathIndex getPathIndex() {
        return pathIndex;
    }

    @Override
    public UnixFSResourceIndex getResourceIndex() {
        return resourceIdIndex;
    }

    @Override
    public LockedRevision lockCurrentRevision() {

        final UnixFSRevisionTableEntry operation = revisionTable
            .reverse()
            .map(slice -> slice.getValue())
            .filter(op -> op.isValid() && op.state.get() == COMMITTED)
            .findFirst()
            .orElseThrow(FatalException::new);

        final UnixFSRevision<?> revision = getRevisionPool().create(operation.revision);

        return new LockedRevision() {

            @Override
            public Revision<?> getRevision() {
                return revision;
            }

            @Override
            public void close() {
                if (operation.readers.decrementAndGet() == 0) {
                    getGarbageCollector().hint(revision);
                }
            }

        };

    }

    @Override
    public PendingRevisionChange beginRevisionUpdate() {
        return new UnixFSPendingRevisionChange();
    }

    @Override
    public Stream<ResourceId> removeAllResources() {
        // TODO Implement this. This should do a fast-nuke of the directory and remove all resources in the storage
        throw new UnsupportedOperationException();
    }

    public UnixFSRevisionPool getRevisionPool() {
        return revisionPool;
    }

    public UnixFSChecksumAlgorithm getPreferredChecksum() {
        return preferredChecksum;
    }

    public UnixFSGarbageCollector getGarbageCollector() {
        return garbageCollector;
    }

    UnixFSTransactionProgramInterpreter.ExecutionHandler newExecutionHandler(
            final NodeId nodeId,
            final Revision<?> revision) {
        return new UnixFSTransactionProgramInterpreter.ExecutionHandler() {

            @Override
            public void unlinkFile(final UnixFSTransactionProgram program, final Path fsPath) {
                utils.doOperationV(() -> Files.delete(fsPath), FatalException::new);
            }

            @Override
            public void unlinkRTPath(final UnixFSTransactionProgram program,
                                     final com.namazustudios.socialengine.rt.Path rtPath) {
                getPathIndex().unlink(revision, nodeId, rtPath);
            }

            @Override
            public void removeResource(final UnixFSTransactionProgram program, final ResourceId resourceId) {
                getResourceIndex().removeResource(revision, resourceId);
            }

            @Override
            public void updateResource(final UnixFSTransactionProgram program,
                                       final Path fsPath,
                                       final ResourceId resourceId) {
                getResourceIndex().updateResource(revision, fsPath, resourceId);
            }

            @Override
            public void linkNewResource(final UnixFSTransactionProgram program,
                                        final Path fsPath,
                                        final ResourceId resourceId) {
                getResourceIndex().linkNewResource(revision, fsPath, resourceId);
            }

            @Override
            public void linkResourceToRTPath(final UnixFSTransactionProgram program,
                                             final ResourceId resourceId,
                                             final com.namazustudios.socialengine.rt.Path rtPath) {
                getPathIndex().link(revision, nodeId, resourceId, rtPath);
            }

        };
    }

    private class UnixFSPendingRevisionChange implements PendingRevisionChange {

        private boolean open = true;

        private boolean destroy = false;

        private final UnixFSRevision<?> revision;

        private final UnixFSRevisionTableEntry storedOperation;

        private final Slice<UnixFSRevisionTableEntry> storedOperationSlice;

        public UnixFSPendingRevisionChange() {

            final UnixFSChecksumAlgorithm preferredAlgorithm = getPreferredChecksum();

            revision = getRevisionPool().createNextRevision();

            storedOperationSlice = revisionTable.nextLeading();

            storedOperation = storedOperationSlice.getValue();
            storedOperation.state.set(WRITING);
            storedOperation.revision.fromRevision(revision);
            storedOperation.algorithm.set(preferredAlgorithm);

            preferredAlgorithm.compute(storedOperation);

        }

        @Override
        public void update() {

            final UnixFSChecksumAlgorithm preferredAlgorithm = getPreferredChecksum();
            final Slice<UnixFSRevisionTableEntry> slice = revisionTable.nextLeading().clear();
            final UnixFSRevisionTableEntry op = slice.getValue();

            destroy = true;

            op.state.set(COMMITTED);
            op.revision.fromRevision(revision);
            op.algorithm.set(preferredAlgorithm);

            preferredAlgorithm.compute(op);

        }

        @Override
        public void fail() {
            storedOperationSlice.clear();
        }

        @Override
        public Revision<?> getRevision() {
            return revision;
        }

        @Override
        public void close() {
            try {
                if (open && destroy) storedOperationSlice.clear();
            } finally {
                open = false;
            }
        }

    }

}
