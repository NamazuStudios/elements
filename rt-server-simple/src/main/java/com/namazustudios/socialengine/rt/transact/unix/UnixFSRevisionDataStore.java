package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.*;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSChecksumAlgorithm.CRC_32;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.StandardOpenOption.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Responsible for tracking the current revision and providing access to the various indices that access the underlying
 * data.
 */
public class UnixFSRevisionDataStore implements RevisionDataStore {

    private static final byte FILLER = (byte) 0xFF;

    public static final String REVISION_BUFFER_COUNT = "com.namazustudios.socialengine.rt.transact.unix.fs.revision.buffer.count";

    /**
     * Some magic bytes int he file to indicate what it is.
     */
    public static final String HEAD_FILE_MAGIC = "RELM";

    /**
     * Constant for major version 1
     */
    public static final int VERSION_MAJOR_1 = 1;

    /**
     * Constant for minor version 0
     */
    public static final int VERSION_MINOR_0 = 0;

    /**
     * Indicates the current major version.
     */
    public static final int VERSION_MAJOR_CURRENT = VERSION_MAJOR_1;

    /**
     * Indicates the current minor version.
     */
    public static final int VERSION_MINOR_CURRENT = VERSION_MINOR_0;

    private static final Logger logger = getLogger(UnixFSRevisionDataStore.class);

    private final UnixFSRevisionDataStoreHeader header;

    private final UnixFSUtils utils;

    private final UnixFSPathIndex pathIndex;

    private final UnixFSResourceIndex resourceIdIndex;

    private final UnixFSRevisionPool revisionPool;

    private final UnixFSGarbageCollector garbageCollector;

    private final MappedByteBuffer  revisionBuffer;

    private final UnixFSCircularBlockBuffer.StructTypedView<UnixFSRevisionDataStoreRevision> circularBlockBuffer;

    private final int revisionBufferCount;

    private final Lock revisionLock = new ReentrantLock();

    private final Condition revisionCondition = revisionLock.newCondition();

    @Inject
    public UnixFSRevisionDataStore(
            final UnixFSUtils utils,
            final UnixFSPathIndex pathIndex,
            final UnixFSResourceIndex resourceIdIndex,
            final UnixFSRevisionPool revisionPool,
            final UnixFSGarbageCollector garbageCollector,
            @Named(REVISION_BUFFER_COUNT) final int revisionBufferCount) throws IOException {

        this.header = new UnixFSRevisionDataStoreHeader();
        this.utils = utils;
        this.pathIndex = pathIndex;
        this.resourceIdIndex = resourceIdIndex;
        this.revisionPool = revisionPool;
        this.garbageCollector = garbageCollector;
        this.revisionBufferCount = revisionBufferCount;

        utils.initialize();
        utils.lockStorageRoot();

        final java.nio.file.Path headFilePath = utils.getHeadFilePath();

        if (isRegularFile(headFilePath)) {
            logger.info("Reading existing journal file {}", headFilePath);
            // TODO Read and Recover Headfile if Necessary
            throw new UnsupportedOperationException("Not yet implemented.");
        } else {
            revisionBuffer = createNewHeadFile(headFilePath);
        }

        revisionBuffer.clear().position(header.size());

        final UnixFSCircularBlockBuffer circularBlockBuffer;
        circularBlockBuffer = new UnixFSCircularBlockBuffer(revisionBuffer, UnixFSRevisionDataStoreRevision.SIZE);

        this.circularBlockBuffer = circularBlockBuffer.forStructType(UnixFSRevisionDataStoreRevision::new);

    }

    private MappedByteBuffer createNewHeadFile(final Path headFilePath) throws IOException {

        try (final FileChannel channel = FileChannel.open(headFilePath, READ, WRITE, CREATE)) {

            final long headerSize = header.size();
            final long totalEntrySize = (UnixFSRevisionData.SIZE * revisionBufferCount);

            final ByteBuffer fillHeader = ByteBuffer.allocate(header.size());
            while(fillHeader.hasRemaining()) fillHeader.put(FILLER);
            fillHeader.rewind();
            channel.write(fillHeader);

            final ByteBuffer fillEntry = ByteBuffer.allocate(UnixFSRevisionData.SIZE);
            while(fillEntry.hasRemaining()) fillEntry.put(FILLER);

            for (int entry = 0; entry < revisionBufferCount; ++entry) {
                fillEntry.rewind();
                channel.write(fillEntry);
            }

            if (channel.size() != (headerSize + totalEntrySize)) {
                // This should only happen if there's an error in the code.
                throw new IllegalStateException("Channel size mismatch!");
            }

            final MappedByteBuffer buffer = channel.map(READ_WRITE, 0, headerSize + totalEntrySize);
            buffer.position(0).limit((int)headerSize);

            final ByteBuffer headerByteBuffer = buffer.slice();
            header.setByteBuffer(headerByteBuffer, 0);
            header.magic.set(HEAD_FILE_MAGIC);
            header.major.set(VERSION_MAJOR_CURRENT);
            header.minor.set(VERSION_MINOR_CURRENT);
            header.revisionBufferCount.set(revisionBufferCount);

            return buffer;

        }

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

    @Override
    public UnixFSRevision<?> getCurrentRevision() {

        final UnixFSRevisionDataStoreRevision data = circularBlockBuffer.trailing();

        try {
            final UnixFSChecksumAlgorithm algorithm = data.algorithm.get();
            algorithm.verify(data);
        } catch (UnixFSChecksumFailureExeception | ArrayIndexOutOfBoundsException ex) {
            throw new FatalException(ex);
        }

        return getRevisionPool().create(data.revision);

    }

    @Override
    public LockedRevision lockCurrentRevision() {
        do {

            final UnixFSRevision<?> revision = getCurrentRevision();

            try {
                garbageCollector.lock(revision);
            } catch (UnixFSRevisionCollectedException ex) {
                logger.trace("Revision was collected. Trying again.", ex);
                continue;
            }

            return new LockedRevision() {
                @Override
                public Revision<?> getRevision() {
                    return revision;
                }

                @Override
                public void close() {
                    garbageCollector.unlock(revision);
                }
            };

        } while(true);
    }

    @Override
    public PendingRevisionChange beginRevisionUpdate() {

        revisionLock.lock();

        final UnixFSRevision<?> next = getRevisionPool().createNextRevision();
        final UnixFSRevisionDataStoreRevision storedRevision = circularBlockBuffer.nextLeading();

        storedRevision.algorithm.set(CRC_32);
        storedRevision.revision.fromRevision(next);
        CRC_32.compute(storedRevision);

        return new PendingRevisionChange() {

            @Override
            public void update() {
                
            }

            @Override
            public void fail() {

            }

            @Override
            public Revision<?> getRevision() {
                return null;
            }

            @Override
            public void close() {
                revisionLock.unlock();
            }

        };

    }

    @Override
    public Stream<ResourceId> removeAllResources() {
        // TODO Figure This Out
        throw new UnsupportedOperationException();
    }

    public UnixFSRevisionPool getRevisionPool() {
        return revisionPool;
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

}
