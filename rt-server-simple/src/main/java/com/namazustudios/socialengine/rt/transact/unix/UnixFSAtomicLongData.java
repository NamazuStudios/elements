package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

import java.nio.ByteBuffer;

/**
 * Used as an means to produce an instance of {@link UnixFSAtomicLong} from the underlying {@link java.nio.ByteBuffer}
 * in the struct. This ensures correct alignment provided the underlying {@link java.nio.ByteBuffer} is properly
 * aligned.
 */
public class UnixFSAtomicLongData extends Struct {

    private final Signed64 value = new Signed64();

    @Override
    public final boolean isPacked() {
        return false;
    }

    /**
     * Returns an {@link UnixFSAtomicLong} which maps to the supplied {@link ByteBuffer}'s position returned by
     * {@link #getByteBufferPosition()}. Note that the returned {@link UnixFSAtomicLong} is permanently bound to the
     * current {@link ByteBuffer}. If a call to {@link #setByteBuffer(ByteBuffer, int)} is made, then a new atomic long
     * will need to be set.
     *
     * @return the {@link UnixFSAtomicLong} instance
     */
    public UnixFSAtomicLong createAtomicLong() {
        final int position = getByteBufferPosition();
        final ByteBuffer byteBuffer = getByteBuffer();
        return UnixFSMemoryUtils.getInstance().getAtomicLong(byteBuffer, position);
    }

    @Override
    public String toString() {
        return "UnixFSAtomicLongData{" +
                "value=" + value +
                '}';
    }

}
