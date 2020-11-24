package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.RevisionDataStore.LockedRevision;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSCircularBlockBuffer.Slice;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSCircularBlockBuffer.View;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSRevisionTableEntry.State.COMMITTED;
import static java.lang.String.format;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Maintains a short list of revisions which are currently uncollected by the garbage collector as well as tracks the
 * current revision. Additionally, this provides a locking mechanism which allows many readers to read a particular
 * revision without having to worry about interference from the garbage collector.
 *
 * As the collection cycle for revisions are processed, the garbage collector will reclaim entries in the revision table
 */
public class UnixFSRevisionTable {

    private static final byte FILLER = (byte) 0xFF;

    public static final String UNIXFS_REVISION_TABLE_COUNT = "com.namazustudios.socialengine.rt.transact.unix.fs.revision.table.count";

    private static final int ACQUIRES_PER_SEMAPHORE = Integer.MAX_VALUE;

    /**
     * Some magic bytes int he file to indicate what it is.
     */
    public static final String REVISION_TABLE_MAGIC = "RELM";

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

    private static final Logger logger = getLogger(UnixFSRevisionTable.class);

    private UnixFSUtils utils;

    private int revisionTableCount;

    private UnixFSRevisionPool revisionPool;

    private UnixFSChecksumAlgorithm checksumAlgorithm;

    private final AtomicReference<Context> context = new AtomicReference<>();

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

        if (context == null) {
            throw new IllegalStateException("Not running.");
        } else {
            context.stop();
        }

    }

    /**
     * Gets the next leading {@link UnixFSRevisionTableEntry} from the revision table and returns it in a write-locked
     * state.
     *
     * @return the next leading {@link UnixFSRevisionTable}
     */
    public RevisionMonitor<Slice<UnixFSRevisionTableEntry>> writeLockNextLeading() {
        return getContext().writeLockNextLeading();
    }

    /**
     * Returns a {@link LockedRevision} for the latest read committed version.
     *
     * @return a {@link RevisionMonitor} which will lock the latest read revision
     */
    public RevisionMonitor<Slice<UnixFSRevisionTableEntry>> readLockLatestReadCommitted() {
        return getContext().readLockLatestReadCommitted();
    }

    /**
     * Scans all revisions, searching for revisions that are collectible. This returns a
     * {@link RevisionMonitor<List<Slice<UnixFSRevisionTableEntry>>} for all collectible {@link UnixFSRevision<?>}
     * instances.
     *
     * @return a {@link List<Slice<UnixFSRevisionTableEntry>>} of all revision entries which may be eligible for collection
     */
    public RevisionMonitor<List<Slice<UnixFSRevisionTableEntry>>> writeLockCollectibleRevisions() {
        return getContext().writeLockCollectibleRevisions();
    }

    /**
     * Updates the supplied {@link Slice<UnixFSRevisionTableEntry>} to be the latest read committed version. Note that
     * the actual supplied.
     *
     * @param slice the slice
     * @return the actual updated revision.
     */
    public UnixFSRevision<?> updateReadCommitted(final Slice<UnixFSRevisionTableEntry> slice) {
        return getContext().updateReadCommitted(slice);
    }

    /**
     * Reclaims the invalid {@link UnixFSRevisionTableEntry} instances on the trailing end, returning them to the pool
     * such that they may be used again later.
     */
    public void reclaimInvalidEntries() {
        getContext().reclaimInvalidEntries();
    }

    public int size() {
        return getContext().size();
    }

    private Context getContext() {
        final Context context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    public UnixFSUtils getUtils() {
        return utils;
    }

    @Inject
    public void setUtils(final UnixFSUtils utils) {
        this.utils = utils;
    }

    public int getRevisionTableCount() {
        return revisionTableCount;
    }

    @Inject
    public void setRevisionTableCount(@Named(UNIXFS_REVISION_TABLE_COUNT) int revisionTableCount) {
        this.revisionTableCount = revisionTableCount;
    }

    public UnixFSRevisionPool getRevisionPool() {
        return revisionPool;
    }

    @Inject
    public void setRevisionPool(UnixFSRevisionPool revisionPool) {
        this.revisionPool = revisionPool;
    }

    public UnixFSChecksumAlgorithm getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    @Inject
    public void setChecksumAlgorithm(UnixFSChecksumAlgorithm checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }

    private class Context {

        private final List<Semaphore> semaphores;

        private final MappedByteBuffer revisionTableBuffer;

        private final UnixFSRevisionTableHeader header;

        private final View<UnixFSRevisionTableEntry> structView;

        private final UnixFSAtomicLong readCommitted;

        private Context() throws IOException {

            this.header = new UnixFSRevisionTableHeader();

            final long totalBytesRequired =
                (long) header.size() +
                (long) getRevisionTableCount() * (long) UnixFSRevisionTableEntry.SIZE;

            if (totalBytesRequired > Integer.MAX_VALUE) {

                final String msg = format("Revision buffer count too large. (%dx%d)=size > %d",
                        UnixFSRevisionTableEntry.SIZE,
                        getRevisionTableCount(),
                        Integer.MAX_VALUE);

                throw new IllegalArgumentException(msg);

            }

            final UnixFSAtomicLong counter;
            final Path revisionTableFilePath = getUtils().getRevisionTableFilePath();

            if (isRegularFile(revisionTableFilePath)) {

                logger.info("Reading existing revision table {}", revisionTableFilePath);
                revisionTableBuffer = loadRevisionTableFile(revisionTableFilePath);

                counter = header.dualCounter.createAtomicLong();
                revisionTableBuffer.clear().position(header.size());

                final UnixFSCircularBlockBuffer circularBlockBuffer = new UnixFSCircularBlockBuffer(
                    counter,
                    revisionTableBuffer,
                    UnixFSRevisionTableEntry.SIZE
                );

                this.structView = circularBlockBuffer.forStructType(UnixFSRevisionTableEntry::new);
                this.readCommitted = header.readCommitted.createAtomicLong();

            } else {

                final Path temporary = createTempFile(
                    revisionTableFilePath.getParent(),
                    "revision-table",
                    "temp");

                logger.info("Creating new revision table at {}", temporary);
                revisionTableBuffer = createRevisionTableFile(temporary);
                revisionTableBuffer.clear().position(header.size());

                counter = header.dualCounter.createAtomicLong();

                final UnixFSCircularBlockBuffer circularBlockBuffer = new UnixFSCircularBlockBuffer(
                    counter,
                    revisionTableBuffer,
                    UnixFSRevisionTableEntry.SIZE
                ).reset();

                this.structView = circularBlockBuffer.forStructType(UnixFSRevisionTableEntry::new);
                this.readCommitted = header.readCommitted.createAtomicLong();

                final Slice<UnixFSRevisionTableEntry> entrySlice = structView.nextLeading();
                this.readCommitted.set(entrySlice.getIndex());

                final UnixFSRevisionTableEntry entry = entrySlice.getValue();
                final UnixFSRevision<?> revision = getRevisionPool().createNextRevision();

                entry.state.set(COMMITTED);
                entry.algorithm.set(getChecksumAlgorithm());
                entry.revision.fromRevision(revision);
                getChecksumAlgorithm().compute(entry);

                if (!getChecksumAlgorithm().isValid(entry)) {
                    throw new FatalException("Invalid entry during creation.");
                }

                logger.info("Revision table initialized. Moving {} -> {}", temporary, revisionTableFilePath);
                move(temporary, revisionTableFilePath);

            }

            final List<Semaphore> semaphores = Stream
                .generate(() -> new Semaphore(ACQUIRES_PER_SEMAPHORE, true))
                .limit(getRevisionTableCount())
                .collect(toList());

            this.semaphores = unmodifiableList(semaphores);

        }

        private MappedByteBuffer loadRevisionTableFile(final Path revisionTableFilePath) throws IOException {

            final MappedByteBuffer mappedByteBuffer;

            final Path temporaryCopy = createTempFile(
                revisionTableFilePath.getParent(),
                "revision-table",
                "temp");

            copy(revisionTableFilePath, temporaryCopy, REPLACE_EXISTING);

            try (final FileChannel channel = open(temporaryCopy, READ, WRITE)) {

                final long headerSize = header.size();
                final long channelSize = channel.size();

                if (channelSize <= headerSize) {
                    final String msg = format("Header table less than expected size %d<=%d", channelSize, headerSize);
                    throw new FatalException(msg);
                }

                final MappedByteBuffer originalMappedByteBuffer = channel.map(READ_WRITE, 0, channelSize);
                header.setByteBuffer(originalMappedByteBuffer, 0);

                final String magic = header.magic.get();
                final int major = header.major.get();
                final int minor = header.minor.get();
                final int revisionTableCount = header.revisionTableCount.get();

                if (!REVISION_TABLE_MAGIC.equals(magic)) {
                    final String msg = format("Unexpected magic!=expected %s!=%s", REVISION_TABLE_MAGIC, magic);
                    throw new FatalException(msg);
                }

                if (VERSION_MAJOR_CURRENT != major || VERSION_MINOR_CURRENT != minor) {

                    final String msg = format("Unsupported version %d.%d!=%d%d",
                            VERSION_MAJOR_CURRENT, VERSION_MINOR_CURRENT,
                            major, minor
                    );

                    throw new FatalException(msg);
                }

                if (getRevisionTableCount() < revisionTableCount) {

                    final String msg = format(
                            "Cannot reduce pool size from %d to %d",
                            getRevisionTableCount(), revisionTableCount
                    );

                    throw new FatalException(msg);

                } else if (getRevisionTableCount() > revisionTableCount) {

                    // Expand the file to accommodate more revisions
                    final ByteBuffer byteBuffer = ByteBuffer.allocate(UnixFSRevisionTableEntry.SIZE);
                    while (byteBuffer.hasRemaining()) byteBuffer.put(FILLER);

                    final int toAdd = getRevisionTableCount() - revisionTableCount;

                    for (int i = 0; i < toAdd; ++i) {
                        byteBuffer.rewind();
                        channel.write(byteBuffer);
                    }

                    mappedByteBuffer = channel.map(READ_WRITE, 0, channel.size());

                } else {
                    // File works as-is, no changes necessary
                    mappedByteBuffer = originalMappedByteBuffer;
                }

                move(temporaryCopy, revisionTableFilePath, ATOMIC_MOVE, REPLACE_EXISTING);
                delete(temporaryCopy);

            }

            return mappedByteBuffer;
        }

        private MappedByteBuffer createRevisionTableFile(final Path revisionTableFilePath) throws IOException {

            try (final FileChannel channel = open(revisionTableFilePath, READ, WRITE, CREATE)) {

                final int headerSize = header.size();
                final int totalEntrySize = (UnixFSRevisionTableEntry.SIZE * getRevisionTableCount());

                // Places the header at the beginning of the file.

                final ByteBuffer fillHeader = ByteBuffer.allocate(headerSize);
                while(fillHeader.hasRemaining()) fillHeader.put(FILLER);
                fillHeader.rewind();
                channel.write(fillHeader);

                // We next allocate memory in the file for the remaining block entries.

                final ByteBuffer fillEntry = ByteBuffer.allocate(UnixFSRevisionTableEntry.SIZE);
                while(fillEntry.hasRemaining()) fillEntry.put(FILLER);

                for (int entry = 0; entry < getRevisionTableCount(); ++entry) {
                    fillEntry.rewind();
                    channel.write(fillEntry);
                }

                if (channel.size() != (headerSize + totalEntrySize)) {
                    // This should only happen if there's an error in the code.
                    throw new IllegalStateException("Channel size mismatch!");
                }

                final MappedByteBuffer buffer = channel.map(READ_WRITE, 0, headerSize + totalEntrySize);
                buffer.position(0).limit(headerSize);

                final ByteBuffer headerByteBuffer = buffer.slice();
                header.setByteBuffer(headerByteBuffer, 0);
                header.magic.set(REVISION_TABLE_MAGIC);
                header.major.set(VERSION_MAJOR_CURRENT);
                header.minor.set(VERSION_MINOR_CURRENT);
                header.revisionTableCount.set(getRevisionTableCount());

                return buffer;

            }

        }

        public void stop() {
            revisionTableBuffer.force();
        }

        public Stream<Slice<UnixFSRevisionTableEntry>> stream() {
            return structView.stream();
        }

        public RevisionMonitor<Slice<UnixFSRevisionTableEntry>> readLockLatestReadCommitted() {

            RevisionMonitor<Slice<UnixFSRevisionTableEntry>> monitor;

            do {
                final int index = (int) readCommitted.get();
                monitor = attemptReadLock(index);
            } while (monitor == null);

            return monitor;

        }

        private RevisionMonitor<Slice<UnixFSRevisionTableEntry>> attemptReadLock(final int index) {

            final Slice<UnixFSRevisionTableEntry> entrySlice = structView.get(index);
            if (entrySlice == null) throw new FatalException("No latest read committed revision.");

            final Semaphore semaphore = semaphores.get(entrySlice.getIndex());

            try {
                // Acquire the semaphore and check it.
                semaphore.acquire(2);
            } catch (InterruptedException e) {
                throw new FatalException(e);
            } finally {
                semaphore.release();
            }

            // If the revision is indeed not valid, then we try again.

            if (!entrySlice.getValue().isValid()) {
                semaphore.release();
                return null;
            }

            return new RevisionMonitor<>() {

                @Override
                public Slice<UnixFSRevisionTableEntry> getScope() {
                    return entrySlice;
                }

                @Override
                public void close() {
                    semaphore.release();
                }

            };

        }

        public RevisionMonitor<Slice<UnixFSRevisionTableEntry>> writeLockNextLeading() {
            final Slice<UnixFSRevisionTableEntry> entrySlice = structView.nextLeading();
            return writeLockRevision(entrySlice);
        }

        private RevisionMonitor<Slice<UnixFSRevisionTableEntry>> writeLockRevision(final Slice<UnixFSRevisionTableEntry> entrySlice) {

            final Semaphore semaphore = semaphores.get(entrySlice.getIndex());

            try {
                semaphore.acquire(ACQUIRES_PER_SEMAPHORE);
            } catch (InterruptedException e) {
                throw new FatalException(e);
            }

            return new RevisionMonitor<Slice<UnixFSRevisionTableEntry>>() {

                @Override
                public Slice<UnixFSRevisionTableEntry> getScope() {
                    return entrySlice;
                }

                @Override
                public void close() {
                    semaphore.release(ACQUIRES_PER_SEMAPHORE);
                }

            };

        }

        public RevisionMonitor<List<Slice<UnixFSRevisionTableEntry>>> writeLockCollectibleRevisions() {

            final List<Semaphore> semaphores = new ArrayList<>();
            final List<Slice<UnixFSRevisionTableEntry>> slices = new ArrayList<>();
            final Slice<UnixFSRevisionTableEntry> readCommittedSlice = structView.get((int)readCommitted.get());;

            for (final Slice<UnixFSRevisionTableEntry> slice : structView.stream().collect(toList())) {

                // If we hit the first read committed revision, we break the loop to avoid collecting the latest
                if (slice.equals(readCommittedSlice)) break;

                // If we can't acquire all readers for the semaphore, we break the loop because we can't safely clear
                // this version.

                final Semaphore semaphore = this.semaphores.get(slice.getIndex());
                if (!semaphore.tryAcquire(ACQUIRES_PER_SEMAPHORE)) break;

                slices.add(slice);
                semaphores.add(semaphore);

            }

            return new RevisionMonitor<List<Slice<UnixFSRevisionTableEntry>>>() {

                @Override
                public List<Slice<UnixFSRevisionTableEntry>> getScope() {
                    return slices;
                }

                @Override
                public void close() {
                    semaphores.forEach(s -> s.release(ACQUIRES_PER_SEMAPHORE));
                }

            };

        }

        public UnixFSRevision<?> updateReadCommitted(final Slice<UnixFSRevisionTableEntry> slice) {

            final UnixFSRevision<?> updateRevision = getRevisionPool().create(slice.getValue().revision);

            Slice<UnixFSRevisionTableEntry> existingEntrySlice;

            do {

                existingEntrySlice = structView.get((int)readCommitted.get());

                final UnixFSRevision<?> existingRevision = create(existingEntrySlice.getValue());

                if (existingRevision.isAfter(updateRevision)) {
                    // The update is later than the existing revision, so we skip the update and leave it in place
                    // but we know the revision supplied would have been properly written.
                    return existingRevision;
                }

            } while (!readCommitted.compareAndSet(existingEntrySlice.getIndex(), slice.getIndex()));

            return updateRevision;

        }

        public UnixFSRevision<?> create(final UnixFSRevisionTableEntry unixFSRevisionTableEntry) {
            return getRevisionPool().create(unixFSRevisionTableEntry.revision);
        }

        public void reclaimInvalidEntries() {
            structView.incrementTrailingUntil(s -> !s.getValue().isValid());
        }

        public int size() {
            return structView.size();
        }

    }

    /**
     * Locks a entry in the revision table. Once closed, the lock is released. The type of lock is specified by the
     * method that returns the monitor.
     */
    public interface RevisionMonitor<LockedT> extends AutoCloseable {

        /**
         * Returns the {@link Slice<UnixFSRevisionTableEntry>} for the backing entry.
         * @return
         */
        LockedT getScope();

        /**
         * Closes this, and releases any read locks on the underlying entry in the revision table.
         */
        void close();

    }

}
