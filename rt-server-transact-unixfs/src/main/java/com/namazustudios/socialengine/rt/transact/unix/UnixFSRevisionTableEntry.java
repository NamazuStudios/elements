package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

class UnixFSRevisionTableEntry extends Struct implements UnixFSChecksumAlgorithm.Checkable {

    static int SIZE = new UnixFSRevisionTableEntry().size();

    final AtomicInteger readers = new AtomicInteger();

    final Unsigned32 checksum = new Unsigned32();

    final Enum8<UnixFSChecksumAlgorithm> algorithm = new Enum8<>(UnixFSChecksumAlgorithm.values());

    final UnixFSRevisionData revision = inner(new UnixFSRevisionData());

    final Enum8<State> state = new Enum8<>(State.values());

    public boolean isValid() {

        final UnixFSChecksumAlgorithm algorithm;

        try {
            algorithm = this.algorithm.get();
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }

        return algorithm.isValid(this);

    }

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

    /**
     * Indicates the state of the revision operation.
     */
    enum State {

        /**
         *
         */
        WRITING,

        /**
         * Indicates that the revision has been committed.
         */
        COMMITTED

    }

}
