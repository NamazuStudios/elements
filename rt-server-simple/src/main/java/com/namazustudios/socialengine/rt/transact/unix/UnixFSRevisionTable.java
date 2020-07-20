package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.transact.FatalException;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSCircularBlockBuffer.StructTypedView;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.*;
import static org.slf4j.LoggerFactory.getLogger;

public class UnixFSRevisionTable {

    private static final byte FILLER = (byte) 0xFF;

    public static final String REVISION_TABLE_COUNT = "com.namazustudios.socialengine.rt.transact.unix.fs.revision.table.count";

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

    private final UnixFSUtils utils;

    private final int revisionTableCount;

    private final AtomicReference<Context> context = new AtomicReference<>();

    @Inject
    public UnixFSRevisionTable(final UnixFSUtils utils,
                               final UnixFSChecksumAlgorithm preferredChecksum,
                               @Named(REVISION_TABLE_COUNT) final int revisionTableCount) throws IOException {
        this.utils = utils;
        this.revisionTableCount = revisionTableCount;
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

        if (context == null) {
            throw new IllegalStateException("Not running.");
        } else {
            context.stop();
        }

    }

    private Context getContext() {
        final Context context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    public Stream<UnixFSCircularBlockBuffer.Slice<UnixFSRevisionTableEntry>> stream() {
        return getContext().stream();
    }

    public Stream<UnixFSCircularBlockBuffer.Slice<UnixFSRevisionTableEntry>> reverse() {
        return getContext().reverse();
    }

    public UnixFSCircularBlockBuffer.Slice<UnixFSRevisionTableEntry> nextLeading() {
        return getContext().nextLeading();
    }

    public UnixFSUtils getUtils() {
        return utils;
    }

    public int getRevisionTableCount() {
        return revisionTableCount;
    }

    private class Context {

        private final MappedByteBuffer revisionTableBuffer;

        private final UnixFSRevisionTableHeader header;

        private final StructTypedView<UnixFSRevisionTableEntry> circularBlockBuffer;

        private Context() throws IOException {

            this.header = new UnixFSRevisionTableHeader();

            final long totalBytesRequired =
                (long) header.size() +
                (long) revisionTableCount * (long) UnixFSRevisionData.SIZE;

            if (totalBytesRequired > Integer.MAX_VALUE) {

                final String msg = format("Revision buffer count too large. (%dx%d)=size > %d",
                        UnixFSRevisionData.SIZE,
                        revisionTableCount,
                        Integer.MAX_VALUE);

                throw new IllegalArgumentException(msg);
            }

            final Path revisionTableFilePath = utils.getRevisionTableFilePath();

            if (isRegularFile(revisionTableFilePath)) {
                logger.info("Reading existing head file {}", revisionTableFilePath);
                revisionTableBuffer = loadRevisionTableFile(revisionTableFilePath);
            } else {
                logger.info("Creating new head file at {}", revisionTableFilePath);
                revisionTableBuffer = createRevisionTableFile(revisionTableFilePath);
            }

            revisionTableBuffer.clear().position(header.size());

            final UnixFSAtomicLong counter = header.atomicLongData.createAtomicLong();

            this.circularBlockBuffer =
                    new UnixFSCircularBlockBuffer(counter, revisionTableBuffer, UnixFSRevisionTableEntry.SIZE)
                            .forStructType(UnixFSRevisionTableEntry::new);

        }

        private MappedByteBuffer loadRevisionTableFile(final Path revisionTableFilePath) throws IOException {

            final MappedByteBuffer mappedByteBuffer;

            final Path temporaryCopy = copy(revisionTableFilePath, revisionTableFilePath.resolveSibling(".tmp"));

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


            }

            return mappedByteBuffer;
        }

        private MappedByteBuffer createRevisionTableFile(final Path revisionTableFilePath) throws IOException {

            try (final FileChannel channel = open(revisionTableFilePath, READ, WRITE, CREATE)) {

                final int headerSize = header.size();
                final int totalEntrySize = (UnixFSRevisionData.SIZE * revisionTableCount);

                // Places the header at the beginning of the file.

                final ByteBuffer fillHeader = ByteBuffer.allocate(headerSize + Long.BYTES);
                while(fillHeader.hasRemaining()) fillHeader.put(FILLER);
                fillHeader.rewind();
                channel.write(fillHeader);

                // We next allocate memory in the file for the remaining block entries.

                final ByteBuffer fillEntry = ByteBuffer.allocate(UnixFSRevisionData.SIZE);
                while(fillEntry.hasRemaining()) fillEntry.put(FILLER);

                for (int entry = 0; entry < revisionTableCount; ++entry) {
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
                header.revisionTableCount.set(revisionTableCount);

                return buffer;

            }

        }

        public void stop() {
            revisionTableBuffer.force();
        }

        public Stream<UnixFSCircularBlockBuffer.Slice<UnixFSRevisionTableEntry>> stream() {
            return circularBlockBuffer.stream();
        }

        public Stream<UnixFSCircularBlockBuffer.Slice<UnixFSRevisionTableEntry>> reverse() {
            return circularBlockBuffer.reverse();
        }

        public UnixFSCircularBlockBuffer.Slice<UnixFSRevisionTableEntry> nextLeading() {
            return circularBlockBuffer.nextLeading();
        }

    }

}
