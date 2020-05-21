package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

import java.nio.ByteBuffer;

public class UnixFSTransactionProgram {

    final ByteBuffer byteBuffer;

    final Header header = new Header();

    UnixFSTransactionProgram(final ByteBuffer byteBuffer, final int programPosition) {
        this.byteBuffer = byteBuffer;
        header.setByteBuffer(byteBuffer, programPosition);
    }

    public void commit() {
        header.algorithm.get().compute(this);
    }

    static class Header extends Struct {

        public static final int SIZE = new Header().size();

        final Enum8<UnixFSChecksumAlgorithm> algorithm = new Enum8<>(UnixFSChecksumAlgorithm.values());

        final Unsigned32 checksum = new Unsigned32();

        final Unsigned32 length = new Unsigned32();

    }

    @FunctionalInterface
    public interface CommandWriter {

        void write(ByteBuffer buffer);

    }

}
