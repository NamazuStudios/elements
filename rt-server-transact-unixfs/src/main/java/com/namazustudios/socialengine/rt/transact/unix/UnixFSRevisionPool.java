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
import static java.nio.file.StandardOpenOption.*;

/**
 * Manages a circular buffer of available revisions, tracks the reference revision, and ensures that all revisions
 * properly sort at any given time. This further safeguards against creating invalid or out of range revisions.
 *
 */
public class UnixFSRevisionPool implements Revision.Factory {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSRevisionPool.class);

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

    private int poolSize;

    private UnixFSUtils utils;

    private final AtomicReference<Context> context = new AtomicReference<>();

    public void start() {

        final Context context = getUtils().doOperation(Context::new, InternalException::new);

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

    public UnixFSUtils getUtils() {
        return utils;
    }

    @Inject
    public void setUtils(final UnixFSUtils utils) {
        this.utils = utils;
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
     * Creates a {@link UnixFSRevision <?>} from the supplied {@link UnixFSRevisionData} (serialized form).
     *
     * @param unixFSRevisionData the {@link UnixFSRevisionData} representing the serialized form
     * @return the newly created {@link UnixFSRevision <?>}
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

        private final UnixFSAtomicLong counter;

        private final UnixFSRevisionPoolData revisionPoolData = new UnixFSRevisionPoolData();

        private Context() throws IOException {

            final Path revisionPoolPath = getUtils().getRevisionPoolPath();

            if (isRegularFile(revisionPoolPath)) {
                logger.info("Reading existing revision pool {}", revisionPoolPath);
                poolBuffer = readRevisionPoolFile(revisionPoolPath);
                counter = revisionPoolData.counter.createAtomicLong();
            } else {
                logger.info("Creating new revision pool at {}", revisionPoolPath);
                poolBuffer = createNewRevisionPoolFile(revisionPoolPath);
                counter = revisionPoolData.counter.createAtomicLong();
                counter.set(0);
            }

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

            }

            return mappedByteBuffer;

        }

        private MappedByteBuffer createNewRevisionPoolFile(final Path revisionPoolPath) throws IOException {

            final MappedByteBuffer mappedByteBuffer;

            try (final FileChannel fileChannel = FileChannel.open(revisionPoolPath, READ, WRITE, CREATE)) {
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

            long revision;

            do {

                revision = counter.get();

                if (Long.compareUnsigned(0xFFFFFFFFFFFFFFFFl, revision) == 0) {
                    // This should never happen, but we want to catch it if it does. A 64-bit long integer would take
                    // approximately 300 years to roll over if one revision was generated once per nanosecond, so  it
                    // is very unlikely that this will ever exhaust the pool. If we do run into this problem because I
                    // made a math error, then we will convert this code to use a BigInteger and take a different
                    // approach
                    throw new FatalException("Exhausted revision pool: 0xFFFFFFFFFFFFFFFFl");
                }

            } while (!counter.compareAndSet(revision, revision + 1));

            return new UnixFSRevision<>(revision);

        }

        public UnixFSRevision<?> create(final UnixFSRevisionData unixFSRevisionData) {
            final long value = unixFSRevisionData.value.get();
            return new UnixFSRevision<>(value);
        }

        public UnixFSRevision<?> create(final String at) {
            final long value = Long.parseLong(at, 16);
            return new UnixFSRevision<>(value);
        }

    }

}
