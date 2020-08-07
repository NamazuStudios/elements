package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.RevisionDataStore;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;

public class UnixFSTransactionJournal implements TransactionJournal {

    private static final byte FILLER = (byte) 0xFF;

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTransactionJournal.class);

    /**
     * The size of each transaction entry.  This is a fixed size.  If a transaction attempts to write more bytes than
     * the size allows, then an exception will result.
     */
    public static final String TRANSACTION_BUFFER_SIZE = "com.namazustudios.socialengine.rt.transact.journal.buffer.size";

    /**
     * The journal buffer count.  This should be large enough to accommodate all running transactions.  The larger this
     * number the better as it will give the journal sufficient breathing room for processing transactions.
     */
    public static final String TRANSACTION_BUFFER_COUNT = "com.namazustudios.socialengine.rt.transact.journal.buffer.count";

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

    private long txnBufferSize;

    private long txnBufferCount;

    private UnixFSUtils utils;

    private UnixFSPathIndex unixFSPathIndex;

    private UnixFSChecksumAlgorithm preferredChecksumAlgorithm;

    private Provider<UnixFSTransactionProgramBuilder> programBuilderProvider;

    private UnixFSRevisionDataStore revisionDataStore;

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

        if (context != null) {
            context.stop();
            logger.info("Stopped.");
        } else {
            throw new IllegalStateException("Not running.");
        }

    }

    @Override
    public UnixFSJournalMutableEntry newMutableEntry(final NodeId nodeId) {
        return getContext().newMutableEntry(nodeId);
    }

    private Context getContext() {
        final Context context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    public long getTxnBufferSize() {
        return txnBufferSize;
    }

    @Inject
    public void setTxnBufferSize(@Named(TRANSACTION_BUFFER_SIZE) long txnBufferSize) {
        if (txnBufferSize > Integer.MAX_VALUE) throw new IllegalArgumentException("tnx buffer size too large");
        this.txnBufferSize = txnBufferSize;
    }

    public long getTxnBufferCount() {
        return txnBufferCount;
    }

    @Inject
    public void setTxnBufferCount(@Named(TRANSACTION_BUFFER_COUNT) long txnBufferCount) {
        if (txnBufferCount > Integer.MAX_VALUE) throw new IllegalArgumentException("tnx buffer count too large");
        this.txnBufferCount = txnBufferCount;
    }

    public UnixFSUtils getUtils() {
        return utils;
    }

    @Inject
    public void setUtils(final UnixFSUtils utils) {
        this.utils = utils;
    }

    public UnixFSPathIndex getUnixFSPathIndex() {
        return unixFSPathIndex;
    }

    @Inject
    public void setUnixFSPathIndex(final UnixFSPathIndex unixFSPathIndex) {
        this.unixFSPathIndex = unixFSPathIndex;
    }

    public UnixFSChecksumAlgorithm getPreferredChecksumAlgorithm() {
        return preferredChecksumAlgorithm;
    }

    @Inject
    public void setPreferredChecksumAlgorithm(final UnixFSChecksumAlgorithm preferredChecksumAlgorithm) {
        this.preferredChecksumAlgorithm = preferredChecksumAlgorithm;
    }

    public Provider<UnixFSTransactionProgramBuilder> getProgramBuilderProvider() {
        return programBuilderProvider;
    }

    @Inject
    public void setProgramBuilderProvider(final Provider<UnixFSTransactionProgramBuilder> programBuilderProvider) {
        this.programBuilderProvider = programBuilderProvider;
    }

    public UnixFSRevisionDataStore getRevisionDataStore() {
        return revisionDataStore;
    }

    @Inject
    public void setRevisionDataStore(final UnixFSRevisionDataStore revisionDataStore) {
        this.revisionDataStore = revisionDataStore;
    }

    public Stream<UnixFSCircularBlockBuffer.Slice<ByteBuffer>> entries() {
        return getContext().circularBlockBuffer.stream();
    }

    private class Context {

        // Created during init

        private final UnixFSJournalHeader header = new UnixFSJournalHeader();

        private final Map<Object, Object> lockedResources = new ConcurrentHashMap<>();

        // Set in constructor

        private final MappedByteBuffer journalBuffer;

        private final UnixFSCircularBlockBuffer circularBlockBuffer;

        private Context() throws IOException {

            final UnixFSAtomicLong counter;
            final Path journalPath = getUtils().getTransactionJournalPath();

            if (isRegularFile(journalPath)) {
                logger.info("Reading existing journal file {}", journalPath);
                journalBuffer = readExistingJournal(journalPath);
                counter = header.counter.createAtomicLong();
            } else {
                logger.info("Creating new journal file {}", journalPath);
                journalBuffer = createNewJournal(journalPath);
                counter = header.counter.createAtomicLong();
            }

            journalBuffer.clear().position(header.size());
            circularBlockBuffer = new UnixFSCircularBlockBuffer(counter, journalBuffer, (int) txnBufferSize);

        }

        private MappedByteBuffer readExistingJournal(final Path journalPath) throws IOException {

            final MappedByteBuffer mappedByteBuffer;

            final Path temporaryCopy = createTempFile(
                journalPath.getParent(),
                "journal",
                "temp");

            copy(temporaryCopy, temporaryCopy, REPLACE_EXISTING);

            try (final FileChannel channel = open(temporaryCopy, READ, WRITE)) {

                final long headerSize = header.size();
                final long channelSize = channel.size();

                if (channelSize < headerSize) {
                    final String msg = format("Journal file less than expected size %d<=%d", channelSize, headerSize);
                    throw new FatalException(msg);
                }

                final MappedByteBuffer originalMappedByteBuffer = channel.map(READ_WRITE, 0, channelSize);
                header.setByteBuffer(originalMappedByteBuffer, 0);

                final String magic = header.magic.get();
                final int major = header.major.get();
                final int minor = header.minor.get();
                final long txnBufferSize = header.txnBufferSize.get();
                final long txnBufferCount = header.txnBufferCount.get();

                if (!JOURNAL_MAGIC.equals(magic)) {
                    final String msg = format("Unexpected magic!=expected %s!=%s", JOURNAL_MAGIC, magic);
                    throw new FatalException(msg);
                }

                if (VERSION_MAJOR_CURRENT != major || VERSION_MINOR_CURRENT != minor) {

                    final String msg = format("Unsupported version %d.%d!=%d%d",
                            VERSION_MAJOR_CURRENT, VERSION_MINOR_CURRENT,
                            major, minor
                    );

                    throw new FatalException(msg);

                }

                if (getTxnBufferSize() < txnBufferSize) {

                    final String msg = format("Unable to reduce transaction buffer size %d<%d",
                        getTxnBufferSize(),
                        txnBufferSize
                    );

                    throw new FatalException(msg);

                }

                if (getTxnBufferCount() < txnBufferCount) {

                    final String msg = format("Unable to reduce transaction count %d<%d",
                        getTxnBufferCount(),
                        txnBufferCount
                    );

                    throw new FatalException(msg);

                }

                if (getTxnBufferSize() > txnBufferSize || getTxnBufferCount() > txnBufferCount) {

                    logger.info("Expanding journal size Buffer {} -> {} Count {} -> {} ",
                        txnBufferSize, getTxnBufferSize(),
                        txnBufferCount, getTxnBufferCount());

                    final Path newTemporaryJournal = createTempFile(
                        journalPath.getParent(),
                        "journal",
                        "temp"
                    );

                    // Makes a copy buffer and creates a new journal file.
                    final ByteBuffer copyBuf = allocateDirect((int)getTxnBufferSize());
                    while (copyBuf.hasRemaining()) copyBuf.put(FILLER);
                    copyBuf.clear();

                    // Creates the new journal file
                    mappedByteBuffer = createNewJournal(newTemporaryJournal);

                    // Sets both buffers to the same position to begin the migration.
                    mappedByteBuffer.position((int) headerSize);
                    originalMappedByteBuffer.position((int) headerSize);

                    // Copies each entry over byte by byte as long as the originally mapped file has data to
                    // read.
                    while (originalMappedByteBuffer.hasRemaining()) {

                        // Limits the copy operation to the size of the original buffer as not to over read from the
                        // source file.
                        copyBuf.limit((int)txnBufferSize);
                        copyBuf.put(originalMappedByteBuffer);

                        // Puts the new buffer into the newly created journal file.
                        copyBuf.clear();
                        mappedByteBuffer.put(copyBuf);
                    }

                    // Finally, moves the newly allocated file over to path on disk. It also deletes the temporary copy
                    // as that is not really needed anymore.

                    move(newTemporaryJournal, journalPath, ATOMIC_MOVE, REPLACE_EXISTING);
                    delete(temporaryCopy);

                } else {

                    logger.info("Journal file configuration unchanged. Using original journal file.");

                    // Use the original file because nothing has changed
                    mappedByteBuffer = originalMappedByteBuffer;
                    header.setByteBuffer(mappedByteBuffer, 0);
                    header.magic.set(JOURNAL_MAGIC);
                    header.major.set(VERSION_MAJOR_CURRENT);
                    header.minor.set(VERSION_MINOR_CURRENT);
                    header.txnBufferSize.set(getTxnBufferSize());
                    header.txnBufferCount.set(getTxnBufferCount());

                    // Moves the temporary file to the journal
                    move(temporaryCopy, journalPath, ATOMIC_MOVE, REPLACE_EXISTING);

                }

            }

            return mappedByteBuffer;

        }

        private MappedByteBuffer createNewJournal(final Path journalPath) throws IOException {

            logger.info("Creating new journal file at {}", journalPath);

            try (final FileChannel channel = open(journalPath, READ, WRITE, CREATE)) {

                final long headerSize = header.size();
                final long totalEntrySize = (getTxnBufferSize() * getTxnBufferCount());

                final ByteBuffer fillHeader = ByteBuffer.allocate(header.size());
                while(fillHeader.hasRemaining()) fillHeader.put(FILLER);
                fillHeader.rewind();
                channel.write(fillHeader);

                final ByteBuffer fillEntry = ByteBuffer.allocate((int) getTxnBufferSize());
                while(fillEntry.hasRemaining()) fillEntry.put(FILLER);

                for (int entry = 0; entry < getTxnBufferCount(); ++entry) {
                    fillEntry.rewind();
                    channel.write(fillEntry);
                }

                if (channel.size() != (headerSize + totalEntrySize)) {
                    // This should only happen if there's an error in the code.
                    throw new IllegalStateException("Channel size mismatch!");
                }

                final MappedByteBuffer journalBuffer = channel.map(READ_WRITE, 0, headerSize + totalEntrySize);

                journalBuffer.position(0).limit((int)headerSize);

                final ByteBuffer headerBuffer = journalBuffer.slice();
                header.setByteBuffer(headerBuffer, 0);

                header.magic.set(JOURNAL_MAGIC);
                header.major.set(VERSION_MAJOR_CURRENT);
                header.minor.set(VERSION_MINOR_CURRENT);
                header.txnBufferSize.set(getTxnBufferSize());
                header.txnBufferCount.set(getTxnBufferCount());

                return journalBuffer;

            }

        }

        public void stop() {
            journalBuffer.force();
        }

        public UnixFSJournalMutableEntry newMutableEntry(final NodeId nodeId) {

            // Fetches, atomically, the next slice, the revision, and sets an instance of OptimisitcLocking which will
            // be used to track the resources held in contention.

            final UnixFSCircularBlockBuffer.Slice<ByteBuffer> slice = circularBlockBuffer.next();

            // TODO Fix This, we may need to supply the revision from the calling code
            final RevisionDataStore.LockedRevision readRevision = getRevisionDataStore().lockLatestReadUncommitted();
            final UnixFSPessimisticLocking pessimisticLocking = newPessimisticLocking();

            // Sets up a build for the specific slide of the journal file.
            final UnixFSTransactionProgramBuilder builder = getProgramBuilderProvider().get()
                    .withNodeId(nodeId)
                    .withByteBuffer(slice.getValue())
                    .withChecksumAlgorithm(getPreferredChecksumAlgorithm());

            // Chain up all unwind operations appropriately. The andThen is specifically meant to continue chaining each
            // unwind operation and execute the next in the chain. This gives the best possible chance at successfully
            // unwinding the operation, covering all edge cases, while minimizing the chance of data loss.

            final UnixFSUtils.IOOperationV onClose = UnixFSUtils.IOOperationV.begin()
                    .andThen(pessimisticLocking::unlock)
                    .andThen(readRevision::close);

            final UnixFSWorkingCopy workingCopy = new UnixFSWorkingCopy(
                nodeId,
                readRevision.getRevision(),
                getUnixFSPathIndex(),
                pessimisticLocking
            );

            // Finally, we construct the entry, which we will return.
            final UnixFSJournalMutableEntry entry = new UnixFSJournalMutableEntry(
                    getUtils(),
                    builder,
                    workingCopy,
                    onClose
            );

            return entry;

        }


        private UnixFSPessimisticLocking newPessimisticLocking() {

            return new UnixFSPessimisticLocking() {

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
                public void lock(final com.namazustudios.socialengine.rt.Path path) throws TransactionConflictException {
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

}
