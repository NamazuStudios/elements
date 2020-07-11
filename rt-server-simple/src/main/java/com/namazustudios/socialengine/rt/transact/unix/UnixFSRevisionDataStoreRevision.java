package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

import java.nio.ByteBuffer;

class UnixFSRevisionDataStoreRevision extends Struct implements UnixFSChecksumAlgorithm.Checkable {

    static int SIZE = new UnixFSRevisionDataStoreRevision().size();

    final Unsigned32 checksum = new Unsigned32();

    final Enum8<UnixFSChecksumAlgorithm> algorithm = new Enum8<>(UnixFSChecksumAlgorithm.values());

    final UnixFSRevisionData revision = inner(new UnixFSRevisionData());

    @Override
    public Unsigned32 checksum() {
        return checksum;
    }

    @Override
    public ByteBuffer contentsToCheck() {

        final ByteBuffer contents = getByteBuffer();
        final int position = getByteBufferPosition();
        final int limit = position + size();

        try {
            contents.position(position).limit(limit);
        } catch (IllegalArgumentException ex) {
            throw new UnixFSChecksumFailureExeception(ex);
        }

        return contents;

    }

}
