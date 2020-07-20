package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.NodeId;
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


import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.StandardOpenOption.*;

public class UnixFSTransactionJournal implements TransactionJournal {

    private static final byte FILLER = (byte) 0xFF;

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTransactionJournal.class);

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

    private final Map<Object, Object> lockedResources = new ConcurrentHashMap<>();

    // Set in constructor

    private final MappedByteBuffer journalBuffer;

    private final long txnEntryBufferSize;

    private final long txnEntryBufferCount;

    private final UnixFSCircularBlockBuffer circularBlockBuffer;

    // Injected Fields

    private final UnixFSUtils utils;

    private final UnixFSPathIndex unixFSPathIndex;

    private final UnixFSChecksumAlgorithm preferredChecksumAlgorithm;

    private final Provider<UnixFSTransactionProgramBuilder> programBuilderProvider;

    @Inject
    public UnixFSTransactionJournal(
            final UnixFSUtils utils,
            final UnixFSPathIndex unixFSPathIndex,
            final UnixFSChecksumAlgorithm preferredChecksumAlgorithm,
            final Provider<UnixFSTransactionProgramBuilder> programBuilderProvider,
            @Named(TRANSACTION_ENTRY_BUFFER_SIZE) final int txnEntryBufferSize,
            @Named(TRANSACTION_ENTRY_BUFFER_COUNT) final int txnEntryBufferCount) throws IOException {

        this.utils = utils;
        this.unixFSPathIndex = unixFSPathIndex;
        this.txnEntryBufferSize = txnEntryBufferSize;
        this.txnEntryBufferCount = txnEntryBufferCount;
        this.preferredChecksumAlgorithm = preferredChecksumAlgorithm;
        this.programBuilderProvider = programBuilderProvider;

        final java.nio.file.Path journalPath = utils.getTransactionJournalPath();

        if (isRegularFile(journalPath)) {
            logger.info("Reading existing journal file {}", journalPath);
            // TODO Read and Recover Journal if Necessary
            throw new UnsupportedOperationException("Not yet implemented.");
        } else {
            journalBuffer = createNewJournal(journalPath);
        }

        journalBuffer.reset().position(header.size());
        circularBlockBuffer = new UnixFSCircularBlockBuffer(null, journalBuffer, txnEntryBufferSize);

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

    public void start() {

    }

    public void stop() {
        journalBuffer.force();
    }

    @Override
    public UnixFSJournalMutableEntry newMutableEntry(final NodeId nodeId) {

        // Fetches, atomically, the next slice, the revision, and sets an instance of OptimisitcLocking which will
        // be used to track the resources held in contention.

        final UnixFSCircularBlockBuffer.Slice<ByteBuffer> slice = circularBlockBuffer.next();

        // TODO Fix This, we may need to supply the revision from the calling code
        final Revision<?> readRevision = Revision.zero();
        final UnixFSOptimisticLocking optimisticLocking = newOptimisticLocking();

        // Sets up a build for the specific slide of the journal file.
        final UnixFSTransactionProgramBuilder builder = programBuilderProvider.get()
            .withNodeId(nodeId)
            .withByteBuffer(slice.getValue())
            .withChecksumAlgorithm(UnixFSChecksumAlgorithm.ADLER_32);

        // Chain up all unwind operations appropriately. The andThen is specifically meant to continue chaining each
        // unwind operation and execute the next in the chain. This gives the best possible chance at successfully
        // unwinding the operation, covering all edge cases, while minimizing the chance of data loss.

        final UnixFSUtils.IOOperationV onClose = UnixFSUtils.IOOperationV.begin()
            .andThen(optimisticLocking::unlock);

        final UnixFSWorkingCopy workingCopy = new UnixFSWorkingCopy(
            nodeId,
            readRevision,
            unixFSPathIndex,
            optimisticLocking
        );

        // Finally, we construct the entry, which we will return.
        final UnixFSJournalMutableEntry entry = new UnixFSJournalMutableEntry(
            utils,
            builder,
            workingCopy,
            onClose
        );

        // We disable the unlock because we will transfer ownership of the lock to the entry. If it made it this far
        // we know that the entry should be closed at a later time.

        return entry;

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

}
