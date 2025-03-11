package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.FatalException;
import javolution.io.Struct;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static dev.getelements.elements.rt.transact.unix.UnixFSChecksumAlgorithm.ADLER_32;
import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionProgramHeader.TRANSACTION_ID_LENGTH_CURRENT;
import static java.lang.String.format;
import static java.nio.ByteBuffer.allocate;

public class UnixFSDataHeader extends Struct implements UnixFSChecksumAlgorithm.Checkable {

    public static final String TASK_MAGIC = "TELM";

    public static final String REVERSE_PATH_MAGIC = "PELM";

    public static final String RESOURCE_CONTENTS_MAGIC = "RELM";

    public static final int VERSION_MAJOR_1 = 1;

    public static final int VERSION_MINOR_0 = 0;

    public static final int VERSION_MAJOR_CURRENT = VERSION_MAJOR_1;

    public static final int VERSION_MINOR_CURRENT = VERSION_MINOR_0;

    public static final int SIZE = new UnixFSDataHeader().size();

    /**
     * The magic bits of hte file.
     */
    final Struct.UTF8String magic = new Struct.UTF8String(4);

    /**
     * The major version.
     */
    final Struct.Signed32 major = new Struct.Signed32();

    /**
     * The minor version.
     */
    final Struct.Signed32 minor = new Struct.Signed32();

    /**
     * Houses the {@link ResourceId} for the resource.
     */
    final PackedResourceId resourceId = new PackedResourceId();

    /**
     * The checksum of the entire file.
     */
    final Struct.Unsigned32 checksum = new Struct.Unsigned32();

    /**
     * The checksum of just the header itself.
     */
    final Struct.Unsigned32 headerChecksum = new Struct.Unsigned32();

    /**
     * A unique string-based transaction ID. This is a unique identifier for the last transaction which wrote the
     * resource.
     */
    final UTF8String transactionId = new UTF8String(TRANSACTION_ID_LENGTH_CURRENT);

    /**
     * The checksum algorithm
     */
    final Struct.Enum8<UnixFSChecksumAlgorithm> checksumAlgorithm = new Struct.Enum8<>(UnixFSChecksumAlgorithm.values());

    class PackedResourceId extends Member {

        public PackedResourceId() {
            super(ResourceId.getSizeInBytes() * Byte.SIZE, 1);
        }

        public ResourceId get() {
            return ResourceId.resourceIdFromByteBuffer(
                    getByteBuffer(),
                    getByteBufferPosition() + offset()
            );
        }

        public void set(final ResourceId resourceId) {
            resourceId.toByteBuffer(getByteBuffer(), getByteBufferPosition() + offset());
        }

        @Override
        public String toString() {
            try {
                return get().toString();
            } catch (Exception ex) {
                return "<undefined>";
            }
        }

    }

    /**
     * Sets the default values of this {@link UnixFSDataHeader}.
     *
     * @return this instance
     */
    public UnixFSDataHeader setTaskDefaults() {
        return setDefaults(TASK_MAGIC);
    }

    /**
     * Sets the default values of this {@link UnixFSDataHeader}.
     *
     * @return this instance
     */
    public UnixFSDataHeader setResourceContentsDefaults() {
        return setDefaults(RESOURCE_CONTENTS_MAGIC);
    }

    /**
     * Sets the default values of this {@link UnixFSDataHeader}.
     *
     * @return this instance
     */
    public UnixFSDataHeader setReversePathDefaults() {
        return setDefaults(REVERSE_PATH_MAGIC);
    }

    /**
     * Sets the default values of this {@link UnixFSDataHeader}.
     *
     * @return this instance
     */
    private UnixFSDataHeader setDefaults(final String magicValue) {
        magic.set(magicValue);
        major.set(VERSION_MAJOR_CURRENT);
        minor.set(VERSION_MINOR_CURRENT);
        checksum.set(0);
        headerChecksum.set(0);
        checksumAlgorithm.set(ADLER_32);
        transactionId.set("");
        return this;
    }

    @Override
    public Unsigned32 checksum() {
        return headerChecksum;
    }

    /**
     * Fills supplied {@link WritableByteChannel} with a placeholder header.
     *
     * @param writableByteChannel the channel to write
     * @throws IOException
     */
    public static void fill(final WritableByteChannel writableByteChannel) throws IOException {

        final var buffer = allocate(SIZE);
        while (buffer.hasRemaining()) buffer.putInt(0xFF);
        buffer.flip();

        while (buffer.hasRemaining()) {
            if (writableByteChannel.write(buffer) < 0) {
                throw new IOException("Unexpected end of file.");
            }
        }

    }

    /**
     * Writes this {@link UnixFSDataHeader} to disk.
     *
     * @param algorithm the algorithm to use when writing the header.
     * @param writableByteChannel the channel
     * @throws IOException
     */
    public void writeHeader(final UnixFSChecksumAlgorithm algorithm,
                            final WritableByteChannel writableByteChannel) throws IOException {

        final var buffer = contentsToCheck();
        buffer.position(getByteBufferPosition()).limit(getByteBufferPosition() + size());

        headerChecksum.set(0);
        checksumAlgorithm.set(algorithm);
        algorithm.compute(this);

        buffer.position(getByteBufferPosition()).limit(getByteBufferPosition() + size());

        while (buffer.hasRemaining()) {
            if (writableByteChannel.write(buffer) < 0) {
                throw new IOException("Unexpected end of file.");
            }
        }

    }

    /**
     * Reads a {@link UnixFSDataHeader} from a {@link ReadableByteChannel}.
     *
     * @param channel the channel
     * @param magic the expected magic value
     * @return the {@link UnixFSDataHeader}
     * @throws IOException
     */
    public static UnixFSDataHeader loadHeader(final ReadableByteChannel channel, final String magic) throws IOException {

        final var header = new UnixFSDataHeader();
        final var buffer = header.getByteBuffer();

        while (buffer.hasRemaining()) {
            if (channel.read(buffer) < 0) {
                throw new IOException("Unexpected end of file.");
            }
        }

        final var algorithm = header.checksumAlgorithm.get();

        if (!algorithm.isValid(header)) {
            throw new UnixFSChecksumFailureException("Resource header invalid (" + algorithm + ")");
        }

        if (!magic.equals(header.magic.get())) {

            final var message = format(
                    "Incorrect data header %s for %s. Expected %s",
                    header.magic.get(),
                    header.resourceId.get(),
                    RESOURCE_CONTENTS_MAGIC
            );

            throw new FatalException(message);

        }

        return header;

    }

}
