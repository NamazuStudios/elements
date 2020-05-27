package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Monitor;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.TransactionConflictException;
import com.namazustudios.socialengine.rt.transact.TransactionJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Arrays.fill;

public class UnixFSTransactionJournal implements TransactionJournal {

    private static final byte FILLER = (byte) 0xFF;

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTransactionJournal.class);

    /**
     * The path to the journal file, specified as a constructor parameter.
     */
    public static final String JOURNAL_PATH = "com.namazustudios.socialengine.rt.transact.journal.path";

    /**
     * The size of each transaction entry.  This is a fixed size.  If a transaction attempts to write more bytes than
     * the size allows, then an exception will result.
     */
    public static final String TRANSACTION_ENTRY_BUFFER_SIZE = "com.namazustudios.socialengine.rt.transact.journal.buffer.size";

    /**
     * The journal buffer count.  This should be large enough to accommodate all running transactions.  The larger this
     * number the better as it will give the journal sufficient breathing room for processing transactions.
     */
    public static final String TRANSACTION_ENTRY_BUFFER_COUNT = "com.namazustudios.socialengine.rt.transact.journal.buffer.count";

    /**
     * Some magic bytes int he file to indicate what it is.
     */
    public static final String JOURNAL_MAGIC = "JELM";

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

    // Created during init

    private final UnixFSJournalHeader header = new UnixFSJournalHeader();

    private final AtomicLong current = new AtomicLong();

    private final Map<Object, Object> lockedResources = new ConcurrentHashMap<>();

    // Set in constructor

    private final Lock rLock;

    private final Lock wLock;

    private final java.nio.file.Path lockFilePath;

    private final MappedByteBuffer journalBuffer;

    private final long txnEntryBufferSize;

    private final long txnEntryBufferCount;

    private final Slices slices;

    // Injected Fields

    private final UnixFSUtils utils;

    private final UnixFSPathIndex unixFSPathIndex;

    private final UnixFSRevisionPool unixFSRevisionPool;

    private final UnixFSGarbageCollector unixFSGarbageCollector;

    private final Provider<UnixFSTransactionProgramBuilder> programBuilderProvider;

    @Inject
    public UnixFSTransactionJournal(
            final UnixFSUtils utils,
            final UnixFSPathIndex unixFSPathIndex,
            final UnixFSRevisionPool unixFSRevisionPool,
            final UnixFSGarbageCollector unixFSGarbageCollector,
            final Provider<UnixFSTransactionProgramBuilder> programBuilderProvider,
            @Named(TRANSACTION_ENTRY_BUFFER_SIZE) final int txnEntryBufferSize,
            @Named(TRANSACTION_ENTRY_BUFFER_COUNT) final int txnEntryBufferCount) throws IOException {

        this.utils = utils;
        this.unixFSPathIndex = unixFSPathIndex;
        this.unixFSRevisionPool = unixFSRevisionPool;
        this.unixFSGarbageCollector = unixFSGarbageCollector;
        this.txnEntryBufferSize = txnEntryBufferSize;
        this.txnEntryBufferCount = txnEntryBufferCount;
        this.programBuilderProvider = programBuilderProvider;

        final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        this.rLock = rwLock.readLock();
        this.wLock = rwLock.readLock();

        final java.nio.file.Path journalPath = utils.getJournalPath();
        lockFilePath = this.utils.lockPath(journalPath);

        if (isRegularFile(journalPath)) {
            logger.info("Reading existing journal file {}", journalPath);
            // TODO Read and Recover Journal if Necessary
            throw new UnsupportedOperationException("Not yet implemented.");
        } else {
            journalBuffer = createNewJournal(journalPath);
        }

        slices = new Slices(journalBuffer);

    }

    private MappedByteBuffer createNewJournal(final java.nio.file.Path journalPath) throws IOException {

        logger.info("Creating new journal file at {}", journalPath);

        try (final FileChannel channel = FileChannel.open(journalPath, READ, WRITE, CREATE)) {

            final long headerSize = header.size();
            final long totalEntrySize = (txnEntryBufferSize * txnEntryBufferCount);

            final ByteBuffer fillHeader = ByteBuffer.allocate(header.size());
            while(fillHeader.hasRemaining()) fillHeader.put(FILLER);
            fillHeader.rewind();
            channel.write(fillHeader);

            final ByteBuffer fillEntry = ByteBuffer.allocate((int)txnEntryBufferSize);
            while(fillEntry.hasRemaining()) fillEntry.put(FILLER);

            for (int entry = 0; entry < txnEntryBufferCount; ++entry) {
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
            header.magic.set(JOURNAL_MAGIC);
            header.major.set(VERSION_MAJOR_CURRENT);
            header.minor.set(VERSION_MINOR_CURRENT);
            header.txnBufferSize.set(txnEntryBufferSize);
            header.txnBufferCount.set(txnEntryBufferCount);

            return buffer;

        }

    }

    @Override
    public UnixFSJournalEntry newSnapshotEntry() {

        boolean unlock = true;

        try {
            rLock.lock();
            final Revision<?> revision = unixFSRevisionPool.create(current);
            final UnixFSJournalEntry entry = new UnixFSJournalEntry(revision, rLock::unlock);
            unlock = false;
            return entry;
        } finally {
            if (unlock) rLock.unlock();
        }

    }

    @Override
    public UnixFSJournalMutableEntry newMutableEntry() {

        boolean unlock = true;

        try {

            // Take a lock of the read lock. Note that subsequent operations will
            rLock.lock();

            // Fetches, atomically, the next slide, the revision, and sets an instance of OptimisitcLocking which will
            // be used to track the resources held in contention.

            final Slices.Slice slice = slices.next();
            final Revision<?> revision = unixFSRevisionPool.create(current);
            final UnixFSOptimisticLocking optimisticLocking = newOptimisticLocking();

            // Sets up a build for the specific slide of the journal file.
            final UnixFSTransactionProgramBuilder builder = programBuilderProvider.get()
                .withByteBuffer(slice.slice)
                .withChecksumAlgorithm(UnixFSChecksumAlgorithm.ADLER_32);

            // Chain up all unwind operations appropriately. The andThen is specifically meant to continue chaining each
            // unwind operation and execute the next in the chain. This gives the best possible chance at successfully
            // unwinding the operation, covering all edge cases, while minimizing the chance of data loss.

            final UnixFSUtils.IOOperationV onClose = UnixFSUtils.IOOperationV.begin()
                .andThen(() -> buildAndExecute(builder))
                .andThen(slice::close)
                .andThen(optimisticLocking::unlock)
                .andThen(rLock::unlock);

            // Finally, we construct the entry, which we will return.
            final UnixFSJournalMutableEntry entry = new UnixFSJournalMutableEntry(
                revision,
                utils,
                unixFSPathIndex,
                builder,
                onClose,
                optimisticLocking);

            // We disable the unlock because we will transfer ownership of the lock to the entry. If it made it this far
            // we know that the entry should be closed at a later time.

            unlock = false;
            return entry;

        } finally {
            // Conditionally perform the unlock operation.
            if (unlock) rLock.unlock();
        }

    }

    private UnixFSOptimisticLocking newOptimisticLocking() {
        return new UnixFSOptimisticLocking() {

            final List<Object> toRelease = new ArrayList<>();

            private void lock(final Object object) throws TransactionConflictException {

                final Object existing = lockedResources.putIfAbsent(object, this);

                if (existing == null || existing == this) {
                    toRelease.add(object);
                } else {
                    throw new TransactionConflictException();
                }

            }

            @Override
            public void lock(final Path path) throws TransactionConflictException {
                lock((Object)path);
            }

            @Override
            public void lock(final ResourceId resourceId) throws TransactionConflictException {
                lock((Object)resourceId);
            }

            @Override
            public void unlock() {
                lockedResources.keySet().removeAll(toRelease);
            }

        };
    }

    private void buildAndExecute(final UnixFSTransactionProgramBuilder builder) {

    }

    @Override
    public Stream<ResourceId> clear() {

        if (!wLock.tryLock()) throw new IllegalStateException("Operation must be performed in exclusive transaction.");

        try {
            // TODO Clear Journal by moving to other location on disk.
            return null;
        } finally {
            wLock.unlock();
        }

    }

    @Override
    public Monitor getExclusiveMonitor() {

        wLock.lock();

        return new Monitor() {

            @Override
            public void close() {
                wLock.unlock();
            }

            @Override
            public Condition getCondition(final String name) {
                throw new UnsupportedOperationException("Conditions are't supported for this implementation.");
            }

        };

    }

    @Override
    public void close() {
        journalBuffer.force();
        utils.unlockDirectory(lockFilePath);
    }

    private class Slices {

        private final byte[] filler;

        private final ArrayList<ByteBuffer> slices = new ArrayList<>();

        private final UnixFSDualCounter counter = new UnixFSDualCounter((int)txnEntryBufferCount);

        public Slices(final ByteBuffer journalBuffer) {

            filler = new byte[(int)txnEntryBufferSize];
            fill(filler, (byte) 0xFF);

            for (int sliceIndex = 0; sliceIndex < txnEntryBufferCount; ++sliceIndex) {

                final int position;

                journalBuffer
                    .position(position = header.size() + ((int)txnEntryBufferSize * sliceIndex))
                    .limit(position + (int)txnEntryBufferSize);

                slices.add(journalBuffer.slice());

            }

        }

        public Slice next() {
            return new Slice();
        }

        private class Slice implements AutoCloseable {

            private final int index;

            private final ByteBuffer slice;

            public Slice() {
                this.index = counter.incrementAhdGetLeading();
                this.slice = slices.get(this.index);
            }

            @Override
            public void close() {
                slice.clear();
                slice.put(filler);
                counter.incrementAndGetTrailing();
            }

        }

    }

}
