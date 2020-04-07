package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.transact.TransactionJournal;
import javolution.io.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Math.min;
import static java.lang.String.format;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.StandardOpenOption.*;

public class UnixFSTransactionJournal implements TransactionJournal {

    public static final String JOURNAL_PATH = "com.namazustudios.socialengine.rt.transact.journal.path";

    public static final String TRANSACTION_BUFFER_SIZE = "com.namazustudios.socialengine.rt.transact.journal.buffer.size";

    public static final String TRANSACTION_BUFFER_COUNT = "com.namazustudios.socialengine.rt.transact.journal.buffer.count";

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTransactionJournal.class);

    public static final String LOCK_FILE_EXT = ".lock";

    public static final String JOURNAL_MAGIC = "JELM";

    private static final int VERSION_MAJOR = 1;

    private static final int VERSION_MINOR = 0;

    private final Path lockFilePath;

    private final MappedByteBuffer journalBuffer;

    private final JournalHeader header = new JournalHeader();

    private final AtomicLong current = new AtomicLong();

    private final AtomicLong committed = new AtomicLong(-1);

    @Inject
    public UnixFSTransactionJournal(@Named(JOURNAL_PATH) final Path journalPath,
                                    @Named(TRANSACTION_BUFFER_SIZE) final int txnBufferSize,
                                    @Named(TRANSACTION_BUFFER_COUNT) final int txnBufferCount) throws IOException {

        lockFilePath = Paths.get(journalPath.toString() + LOCK_FILE_EXT);

        if (Files.exists(lockFilePath)) {
            final String msg = format("Journal path is locked %s", lockFilePath);
            throw new FileAlreadyExistsException(msg);
        } else {
            Files.createFile(lockFilePath);
        }

        try (final FileChannel channel = FileChannel.open(journalPath, READ, WRITE, CREATE)) {

            final long channelSize = channel.size();
            final long requestedSize = header.size() + (txnBufferSize * txnBufferCount);
            if (channel.size() > Integer.MAX_VALUE) throw new IOException("Journal File too Large");

            final MappedByteBuffer headerByteBuffer = channel.map(READ_WRITE, 0, header.size());

            if (channelSize == 0) {
                header.setByteBuffer(headerByteBuffer, 0);
                header.magic.set(JOURNAL_MAGIC);
                header.major.set(VERSION_MAJOR);
                header.minor.set(VERSION_MINOR);
                header.txnBufferSize.set(txnBufferSize);
                header.txnBufferCount.set(txnBufferCount);
            } else if (channelSize < requestedSize) {
                throw new IOException("Shrinking journal is not supported.");
            } else {

                header.setByteBuffer(headerByteBuffer, 0);

                if (JOURNAL_MAGIC.equals(header.magic.get())) {
                    throw new IOException("Invalid magic bits.");
                } else if (header.major.get() > VERSION_MAJOR) {
                    final String msg = format("Incompatible version %d.%d != %d.%d",
                            header.major.get(), header.major.get(),
                            VERSION_MAJOR, VERSION_MINOR);
                    throw new IOException(msg);
                } else if (header.minor.get() > VERSION_MINOR) {
                    final String msg = format("Incompatible version %d.%d != %d.%d",
                            header.major.get(), header.major.get(),
                            VERSION_MAJOR, VERSION_MINOR);
                    throw new IOException(msg);
                } else if (header.txnBufferSize.get() != txnBufferSize) {
                    final String msg = format("Unable to change transaction buffer size %d -> %d",
                            header.txnBufferSize.get(), txnBufferSize);
                    throw new IOException(msg);
                } else if (header.txnBufferCount.get() < txnBufferCount) {
                    final String msg = format("Unable to shrink transaction buffer count %d -> %d",
                            header.txnBufferCount.get(), txnBufferSize);
                    throw new IOException(msg);
                }

            }

            final byte fillerArray[] = new byte[4096];
            Arrays.fill(fillerArray, (byte)0xFF);

            final ByteBuffer filler = ByteBuffer.wrap(fillerArray);
            int remaining = (int) requestedSize;

            while (remaining > 0) {
                filler.limit(min(remaining, fillerArray.length));
                channel.write(filler);
                filler.rewind();
            }

            journalBuffer = channel.map(READ_WRITE, header.size(), requestedSize);

        }

    }

    @Override
    public Entry getCurrentEntry() {
        // TODO
        return null;
    }

    @Override
    public MutableEntry newEntry() {
        // TODO
        return null;
    }

    @Override
    public void close() {

        journalBuffer.force();

        try {
            deleteIfExists(lockFilePath);
        } catch (IOException e) {
            logger.error("Failed to delete lock file.", e);
        }

    }

    private static class JournalHeader extends Struct {

        public final UTF8String magic = new UTF8String(4);

        public final Signed32 major = new Signed32();

        public final Signed32 minor = new Signed32();

        public final Unsigned32 txnBufferSize = new Unsigned32();

        public final Unsigned32 txnBufferCount = new Unsigned32();

    }

    private static class TransactionHeader extends Struct {

        public final Unsigned32 crc32 = new Unsigned32();

        public final Unsigned32 length = new Unsigned32();

    }

    public static void main(final String[] args) throws Exception {

        final Path path = Paths.get("test.txt");

        try (final UnixFSTransactionJournal j = new UnixFSTransactionJournal(path, 1024 * 1024, 256)) {
            System.out.println("Hello World!");
        }

    }

}
