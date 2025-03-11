package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.transact.DataStore;
import dev.getelements.elements.rt.transact.FatalException;
import dev.getelements.elements.rt.transact.TransactionJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.nio.ByteBuffer.allocate;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.*;

public class UnixFSTransactionJournal implements TransactionJournal {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTransactionJournal.class);

    /**
     * The size of each transaction entry.  This is a fixed size.  If a transaction attempts to write more bytes than
     * the size allows, then an exception will result.
     */
    public static final String UNIXFS_TRANSACTION_BUFFER_SIZE = "dev.getelements.elements.rt.transact.journal.buffer.size";

    /**
     * Some magic bytes in the file to indicate what it is.
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

    private int txnBufferSize;

    private DataStore dataStore;

    private UnixFSUtils utils;

    private UnixFSChecksumAlgorithm preferredChecksumAlgorithm;

    private Provider<UnixFSTransactionProgramBuilder> programBuilderProvider;

    private final AtomicReference<Context> context = new AtomicReference<>();

    public void start() {

        final Context context = utils.doOperation(Context::new, InternalException::new);

        if (this.context.compareAndSet(null, context)) {
            logger.info("Started.");
            context.replay();
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

    public int getTxnBufferSize() {
        return txnBufferSize;
    }

    @Inject
    public void setTxnBufferSize(@Named(UNIXFS_TRANSACTION_BUFFER_SIZE) int txnBufferSize) {
        this.txnBufferSize = txnBufferSize;
    }

    public UnixFSUtils getUtils() {
        return utils;
    }

    @Inject
    public void setUtils(final UnixFSUtils utils) {
        this.utils = utils;
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

    public DataStore getDataStore() {
        return dataStore;
    }

    @Inject
    public void setDataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    private class Context {

        // Created during init

        private final UnixFSJournalHeader header = new UnixFSJournalHeader();

        // Set in constructor

        private final MappedByteBuffer journalBuffer;

        private final UnixFSAtomicLong transactionIdCounter;

        private Context() throws IOException {

            final Path journalPath = getUtils().getTransactionJournalFilePath();

            if (isRegularFile(journalPath)) {
                logger.info("Reading existing journal file {}", journalPath);
                journalBuffer = readExistingJournal(journalPath);
                transactionIdCounter = header.counter.createAtomicLong();
            } else {
                logger.info("Creating new journal file {}", journalPath);
                journalBuffer = createNewJournal(journalPath);
                transactionIdCounter = header.counter.createAtomicLong();
            }

        }

        private MappedByteBuffer readExistingJournal(final Path journalPath) throws IOException {

            final MappedByteBuffer mappedByteBuffer;

            try (final FileChannel channel = open(journalPath, READ, WRITE, SYNC)) {

                final long headerSize = header.size();
                final long channelSize = channel.size();

                if (channelSize < headerSize) {
                    final String msg = format("Journal file less than expected size %d<%d", channelSize, headerSize);
                    throw new FatalException(msg);
                } else if (channelSize > headerSize) {
                    logger.warn("Journal file greater than expected size {}>{}", channelSize, headerSize);
                }

                mappedByteBuffer = channel.map(READ_WRITE, 0, channelSize);
                header.setByteBuffer(mappedByteBuffer, 0);

                final String magic = header.magic.get();
                final int major = header.major.get();
                final int minor = header.minor.get();

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

            }

            return mappedByteBuffer;

        }

        private MappedByteBuffer createNewJournal(final Path journalPath) throws IOException {

            logger.info("Creating new journal file at {}", journalPath);

            try (final FileChannel channel = open(journalPath, READ, WRITE, CREATE, SYNC)) {

                final var fillHeader = header.getByteBuffer();
                fillHeader.flip();

                while (fillHeader.hasRemaining()) {
                    if (channel.write(fillHeader) < 0) {
                        throw new FatalException("Unexpected end of stream writing journal file.");
                    }
                }

                final var journalBuffer = channel.map(READ_WRITE, 0, header.size());
                header.setByteBuffer(journalBuffer, 0);
                header.magic.set(JOURNAL_MAGIC);
                header.major.set(VERSION_MAJOR_CURRENT);
                header.minor.set(VERSION_MINOR_CURRENT);
                header.counter.initialize(0);
                return journalBuffer.force();

            }

        }

        public void stop() {
            journalBuffer.force();
        }

        public UnixFSJournalMutableEntry newMutableEntry(final NodeId nodeId) {

            // Gets the next transaction ID from the journal

            final var transactionId = nextTransactionId();
            final var transactionBuffer = allocate(getTxnBufferSize());
            final var transactionFilePath = getUtils().getTransactionFilePath(transactionId);

            // Sets up a build for the specific slide of the journal file.

            final var builder = getProgramBuilderProvider().get()
                    .withNodeId(nodeId)
                    .withTransactionId(transactionId)
                    .withByteBuffer(transactionBuffer)
                    .withChecksumAlgorithm(getPreferredChecksumAlgorithm());

            final Consumer<UnixFSJournalMutableEntry> onWrite = entry -> getUtils().doOperationV(() -> {
                try (final var output = open(transactionFilePath, WRITE, CREATE_NEW, SYNC, DSYNC)) {

                    transactionBuffer.flip();

                    while (transactionBuffer.hasRemaining()) {
                        if (output.write(transactionBuffer) < 0) {
                            throw new FatalException("Unexpected end of stream: " + transactionFilePath);
                        }
                    }

                }
            });

            final Consumer<UnixFSJournalMutableEntry> onClose = entry -> getUtils().doOperationV(() -> delete(transactionFilePath));

            return new UnixFSJournalMutableEntry(
                    transactionId,
                    getDataStore(),
                    builder,
                    onWrite,
                    onClose
            );

        }

        private String nextTransactionId() {

            long value;

            do {
                value = transactionIdCounter.get();
            } while (!transactionIdCounter.compareAndSet(value, value + 1));

            journalBuffer.force();
            return format("%016X", value + 1);

        }

        public void replay() {
            getUtils().doOperationV(this::doReplay);
        }

        private void doReplay() throws IOException {

            final var journalDPath = getUtils().getTransactionJournalDirectoryPath();

            try (final var journalD = (SecureDirectoryStream<Path>) newDirectoryStream(journalDPath)) {

                final var transactions = new TreeSet<Path>();

                for (var transaction : journalD) {
                    if (transaction.endsWith(UnixFSUtils.TRANSACTION_EXTENSION) && isRegularFile(transaction)) {
                        transactions.add(transaction);
                    } else {
                        logger.warn("Encountered un expected file {}. Ignoring.", transaction);
                    }
                }

                for (var transaction : transactions) {
                    doReplayForTransaction(journalD, transaction);
                }
            }

        }

        private boolean doReplayForTransaction(
                final SecureDirectoryStream<Path> journalD,
                final Path transactionPath) throws IOException {

            final ByteBuffer transactionBuffer;

            try (var transactionFileChannel = open(transactionPath, READ)) {

                transactionBuffer = allocate((int)transactionFileChannel.size());

                while (transactionBuffer.hasRemaining()) {
                    if (transactionFileChannel.read(transactionBuffer) < 0) {
                        throw new FatalException("Unexpected end of stream: " + transactionPath);
                    }
                }

            }

            final var program = new UnixFSTransactionProgram(transactionBuffer);

            if (program.isValid()) {

                final var commitHandler = new UnixFSTransactionCommitExecutionHandler(getDataStore());
                final var cleanupHandler = new UnixFSTransactionRollbackExecutionHandler(getDataStore());

                program.interpreter()
                        .tryExecuteCommitPhase(commitHandler)
                        .tryExecuteCleanupPhase(cleanupHandler);

                journalD.deleteFile(transactionPath);

                return true;
            } else {
                logger.info("Skipping partial transaction {}", transactionPath);
                return false;
            }

        }

    }

}
