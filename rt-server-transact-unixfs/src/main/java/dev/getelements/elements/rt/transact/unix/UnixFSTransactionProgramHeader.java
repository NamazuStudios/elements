package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.id.NodeId;
import javolution.io.Struct;

import java.nio.ByteBuffer;

class UnixFSTransactionProgramHeader extends Struct implements UnixFSChecksumAlgorithm.Checkable {

    public static final int SIZE = new UnixFSTransactionProgramHeader().size();

    final Unsigned32 checksum = new Unsigned32();

    final Enum8<UnixFSChecksumAlgorithm> algorithm = new Enum8<>(UnixFSChecksumAlgorithm.values());

    final PackedNodeId nodeId = new PackedNodeId();

    final Unsigned8 phases = new Unsigned8();

    final Unsigned32 length = new Unsigned32();

    final Unsigned32 commitPos = new Unsigned32();

    final Unsigned32 commitLen = new Unsigned32();

    final Unsigned32 cleanupPos = new Unsigned32();

    final Unsigned32 cleanupLen = new Unsigned32();

    final UnixFSRevisionData revision = inner(new UnixFSRevisionData());

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
            throw new UnixFSChecksumFailureExeception(ex);
        }

        return byteBuffer;

    }

}
