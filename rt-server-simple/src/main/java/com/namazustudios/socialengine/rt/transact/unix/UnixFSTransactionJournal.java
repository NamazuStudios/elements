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

    // Injected Fields

    private final UnixFSUtils utils;

    private final UnixFSRevisionPool unixFSRevisionPool;

    private final UnixFSGarbageCollector unixFSGarbageCollector;

    private final Provider<UnixFSTransactionProgram.Builder> programBuilderProvider;

    @Inject
    public UnixFSTransactionJournal(
            final UnixFSUtils utils,
            final UnixFSRevisionPool unixFSRevisionPool,
            final UnixFSGarbageCollector unixFSGarbageCollector,
            final Provider<UnixFSTransactionProgram.Builder> programBuilderProvider,
            @Named(TRANSACTION_ENTRY_BUFFER_SIZE) final int txnEntryBufferSize,
            @Named(TRANSACTION_ENTRY_BUFFER_COUNT) final int txnEntryBufferCount) throws IOException {

        this.utils = utils;
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

            final MappedByteBuffer headerByteBuffer = channel.map(READ_WRITE, 0, header.size());
            header.setByteBuffer(headerByteBuffer, 0);
            header.magic.set(JOURNAL_MAGIC);
            header.major.set(VERSION_MAJOR_CURRENT);
            header.minor.set(VERSION_MINOR_CURRENT);
            header.txnBufferSize.set(txnEntryBufferSize);
            header.txnBufferCount.set(txnEntryBufferCount);

            return channel.map(READ_WRITE, header.size(), totalEntrySize);

        }

    }

    @Override
    public Entry getCurrentEntry() {

        boolean unlock = true;

        try {
            rLock.lock();
            final Revision<?> nextRevision = unixFSRevisionPool.nextRevision(current);
            final Entry entry = new UnixFSJournalEntry(nextRevision, rLock::unlock);
            unlock = false;
            return entry;
        } finally {
            if (unlock) rLock.unlock();
        }

    }

    @Override
    public MutableEntry newMutableEntry() {

        boolean unlock = true;

        final UnixFSOptimisticLocking optimisticLocking = new UnixFSOptimisticLocking() {

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

        try {
            rLock.lock();
            final Revision<?> nextRevision = unixFSRevisionPool.nextRevision(current);
            final MutableEntry entry = null; // TODO Fix This
            unlock = false;
            return entry;
        } finally {
            if (unlock) rLock.unlock();
        }
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

}
