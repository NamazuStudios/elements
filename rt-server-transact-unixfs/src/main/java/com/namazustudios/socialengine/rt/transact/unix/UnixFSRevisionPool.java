package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Manages a circular buffer of available revisions, tracks the reference revision, and ensures that all revisions
 * properly sort at any given time. This further safeguards against creating invalid or out of range revisions.
 *
 */
public class UnixFSRevisionPool implements Revision.Factory {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSRevisionPool.class);

    public static final String REVISION_POOL_SIZE = "com.namazustudios.socialengine.rt.transact.unix.fs.revision.pool.size";

    /**
     * Some magic bytes int he file to indicate what it is.
     */
    public static final String POOL_FILE_MAGIC = "PELM";

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

    private final UnixFSUtils utils;

    private final int poolSize;

    private final AtomicReference<Context> context = new AtomicReference<>();

    @Inject
    public UnixFSRevisionPool(final UnixFSUtils utils,
                              @Named(REVISION_POOL_SIZE) final int poolSize) {
        this.utils = utils;
        this.poolSize = poolSize;
    }

    public void start() {

        final Context context = utils.doOperation(Context::new, InternalException::new);

        if (this.context.compareAndSet(null, context)) {
            logger.info("Started.");
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    public void stop() {

        final Context context = this.context.getAndSet(null);

        if (context != null) {
            context.stop();
            logger.info("Stopped.");
        } else {
            throw new IllegalStateException("Not running.");
        }

    }

    /**
     * Creates a new {@link Revision<?>} and returns it. Once returned, the {@link Revision<?>} must be either committed
     * or canceled before future {@link Revision<?>}s will be applied
     * @return
     */
    public UnixFSRevision<?> createNextRevision() {
        return getContext().creteNextRevision();
    }

    /**
     * Creates a {@link UnixFSRevision<?>} from the supplied {@link UnixFSRevisionData} (serialized form).
     *
     * @param unixFSRevisionData the {@link UnixFSRevisionData} representing the serialized form
     * @return the newly created {@link UnixFSRevision<?>}
     */
    public UnixFSRevision<?> create(final UnixFSRevisionData unixFSRevisionData) {
        return getContext().create(unixFSRevisionData);
    }

    @Override
    public UnixFSRevision<?> create(final String at) {
        return getContext().create(at);
    }


    private Context getContext() {
        final Context context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    private class Context {

        private final MappedByteBuffer poolBuffer;

        private final UnixFSDualCounter revisionCounter;

        private final UnixFSRevisionPoolData revisionPoolData = new UnixFSRevisionPoolData();

        private Context() throws IOException {

            final Path revisionPoolPath = utils.getRevisionPoolPath();

            if (isRegularFile(revisionPoolPath)) {
                logger.info("Reading existing head file {}", revisionPoolPath);
                poolBuffer = readRevisionPoolFile(revisionPoolPath);
            } else {
                logger.info("Creating new head file at {}", revisionPoolPath);
                poolBuffer = createNewRevisionPoolFile(revisionPoolPath);
            }

            revisionCounter = new UnixFSDualCounter(poolSize, revisionPoolData.atomicLongData.createAtomicLong());

        }


        private MappedByteBuffer readRevisionPoolFile(final Path revisionPoolPath) throws IOException {

            final MappedByteBuffer mappedByteBuffer;

            try (final FileChannel fileChannel = FileChannel.open(revisionPoolPath, READ, WRITE)) {

                final long actual = fileChannel.size();
                final long expected = revisionPoolData.size();

                if (expected != actual) {

                    final String msg = format(
                            "Unexpected revision pool file size. expected!=actual!=%d!=%d",
                            fileChannel.size(), revisionPoolData.size()
                    );

                    throw new FatalException(msg);
                }

                // Loads the file into the struct
                mappedByteBuffer = fileChannel.map(READ_WRITE, 0, revisionPoolData.size());
                revisionPoolData.setByteBuffer(mappedByteBuffer, 0);

                // Verifies the integrity of the struct
                final String magic = revisionPoolData.magic.get();
                final int major = revisionPoolData.major.get();
                final int minor = revisionPoolData.minor.get();
                final int max = revisionPoolData.max.get();

                if (!POOL_FILE_MAGIC.equals(magic)) {
                    final String msg = format("Unexpected magic!=expected %s!=%s",POOL_FILE_MAGIC, magic);
                    throw new FatalException(msg);
                }

                if (VERSION_MAJOR_CURRENT != major || VERSION_MINOR_CURRENT != minor) {

                    final String msg = format("Unsupported version %d.%d!=%d%d",
                            VERSION_MAJOR_CURRENT, VERSION_MINOR_CURRENT,
                            major, minor
                    );

                    throw new FatalException(msg);
                }

                if (poolSize < max) {

                    final String msg = format(
                            "Cannot reduce pool size from %d to %d",
                            magic, poolSize
                    );

                    throw new FatalException(msg);
                }

            }

            return mappedByteBuffer;

        }

        private MappedByteBuffer createNewRevisionPoolFile(final Path revisionPoolPath) throws IOException {

            final MappedByteBuffer mappedByteBuffer;

            try (final FileChannel fileChannel = FileChannel.open(revisionPoolPath, READ, WRITE)) {
                final ByteBuffer buffer = ByteBuffer.allocate(revisionPoolData.size());

                // Writes the initial values to the file and flushes the buffer to disk.

                revisionPoolData.setByteBuffer(buffer, 0);
                revisionPoolData.magic.set(POOL_FILE_MAGIC);
                revisionPoolData.major.set(VERSION_MAJOR_CURRENT);
                revisionPoolData.major.set(VERSION_MINOR_CURRENT);
                revisionPoolData.max.set(poolSize);
                fileChannel.write(buffer);
                fileChannel.force(false);

                // Remaps the revision pool data to read from the mapped buffer.
                mappedByteBuffer = fileChannel.map(READ_WRITE, 0, revisionPoolData.size());
                revisionPoolData.setByteBuffer(mappedByteBuffer, 0);

            }

            return mappedByteBuffer;

        }

        void stop() {
            poolBuffer.force();
        }

        public UnixFSRevision<?> creteNextRevision() {
            return new UnixFSRevision<>(revisionCounter::getTrailing, revisionCounter.incrementLeadingAndGetSnapshot());
        }

        public UnixFSRevision<?> create(final UnixFSRevisionData unixFSRevisionData) {
            return unixFSRevisionData.toRevision(revisionCounter::getTrailing);
        }

        public UnixFSRevision<?> create(final String at) {
            final UnixFSDualCounter.Snapshot snapshot =  UnixFSDualCounter.Snapshot.fromString(at);
            return new UnixFSRevision<>(revisionCounter::getTrailing, snapshot);
        }

    }

}
