package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.NodeId;
import javolution.io.Struct;

import java.nio.ByteBuffer;

class UnixFSTransactionProgramHeader extends Struct implements UnixFSChecksumAlgorithm.Checkable {

    public static final String MAGIC = "TXEL";

    public static final int TRANSACTION_ID_LENGTH_V1_0 = 16;

    public static final int TRANSACTION_ID_LENGTH_CURRENT = TRANSACTION_ID_LENGTH_V1_0;

    public static final int SIZE = new UnixFSTransactionProgramHeader().size();

    public static final int VERSION_MAJOR_1 = 1;

    public static final int VERSION_MINOR_0 = 0;

    public static final int VERSION_MAJOR_CURRENT = VERSION_MAJOR_1;

    public static final int VERSION_MINOR_CURRENT = VERSION_MINOR_0;

    final Struct.UTF8String magic = new Struct.UTF8String(4);

    final Signed32 major = new Signed32();

    final Signed32 minor = new Signed32();

    final Unsigned32 checksum = new Unsigned32();

    final Enum8<UnixFSChecksumAlgorithm> algorithm = new Enum8<>(UnixFSChecksumAlgorithm.values());

    final UTF8String transactionId = new UTF8String(TRANSACTION_ID_LENGTH_CURRENT);

    final Unsigned8 phases = new Unsigned8();

    final Unsigned32 length = new Unsigned32();

    final Unsigned32 commitPos = new Unsigned32();

    final Unsigned32 commitLen = new Unsigned32();

    final Unsigned32 cleanupPos = new Unsigned32();

    final Unsigned32 cleanupLen = new Unsigned32();

    class PackedNodeId extends Member {

        public PackedNodeId() {
            super(NodeId.getSizeInBytes() * Byte.SIZE, 1);
        }

        public NodeId get() {
            return NodeId.nodeIdFromByteBuffer(getByteBuffer(), getByteBufferPosition() + offset());
        }

        public void set(final NodeId nodeId) {
            nodeId.toByteBuffer(getByteBuffer(), getByteBufferPosition() + offset());
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
     * Sets the defaults.
     *
     * @return the defaults
     */
    public UnixFSTransactionProgramHeader setDefaults() {
        magic.set(MAGIC);
        major.set(VERSION_MAJOR_CURRENT);
        minor.set(VERSION_MINOR_CURRENT);
        return this;
    }

    @Override
    public Unsigned32 checksum() {
        return checksum;
    }

    @Override
    public ByteBuffer contentsToCheck() {

        final ByteBuffer byteBuffer = getByteBuffer();
        final int position = getByteBufferPosition();
        final int programLength = (int) length.get();
        final int limit = size() + position + programLength;

        try {
            byteBuffer.position(position).limit(limit);
        } catch (IllegalArgumentException ex) {
            throw new UnixFSChecksumFailureException(ex);
        }

        return byteBuffer;

    }

}
