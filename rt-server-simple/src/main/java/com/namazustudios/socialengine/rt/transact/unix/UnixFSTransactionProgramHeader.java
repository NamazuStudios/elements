package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.NodeId;
import javolution.io.Struct;

class UnixFSTransactionProgramHeader extends Struct {

    public static final int SIZE = new UnixFSTransactionProgramHeader().size();

    final Enum8<UnixFSChecksumAlgorithm> algorithm = new Enum8<>(UnixFSChecksumAlgorithm.values());

    final PackedNodeId nodeId = new PackedNodeId();

    final Unsigned32 checksum = new Unsigned32();

    final Unsigned8 phases = new Unsigned8();

    final Unsigned32 length = new Unsigned32();

    final Unsigned32 commitPos = new Unsigned32();

    final Unsigned32 commitLen = new Unsigned32();

    final Unsigned32 cleanupPos = new Unsigned32();

    final Unsigned32 cleanupLen = new Unsigned32();

    final UnixFSRevisionData revision = inner(new UnixFSRevisionData());

    class PackedNodeId extends Member {

        public PackedNodeId() {
            super(NodeId.getSizeInBytes() * Byte.SIZE, 8);
        }

        public NodeId get() {
            return NodeId.nodeIdFromByteBuffer(getByteBuffer(), getByteBufferPosition());
        }

        public void set(final NodeId nodeId) {
            nodeId.toByteBuffer(getByteBuffer(), getByteBufferPosition());
        }

    }

}
