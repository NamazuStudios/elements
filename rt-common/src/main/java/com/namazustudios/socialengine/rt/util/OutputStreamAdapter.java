package com.namazustudios.socialengine.rt.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import static java.lang.Integer.min;

/**
 * Wraps a {@link WritableByteChannel} in an {@link OutputStream} using a {@link ByteBuffer} as an intermediate buffer.
 */
public class OutputStreamAdapter extends OutputStream {

    /**
     * Indicates if the {@link OutputStream} should flush after every operation. Note that flush will always happen
     * when the adapter is closed.
     */
    public static int FLUSH = 0x1 << 0;

    /**
     * Indicates if the {@link OutputStream} should close the underlying {@link WritableByteChannel}.
     */
    public static int CLOSE = 0x1 << 1;

    private final WritableByteChannel wbc;

    private final ByteBuffer byteBuffer;

    private final Operation ioop;

    private final Operation close;

    public OutputStreamAdapter(final WritableByteChannel wbc, final ByteBuffer byteBuffer, final int options) {
        this.wbc = wbc;
        this.byteBuffer = byteBuffer;
        ioop =  (FLUSH & options) == 0 ? Operation.NOOP : this::doFlush;
        close = (CLOSE & options) == 0 ? Operation.NOOP : wbc::close;
    }

    @Override
    public void write(final int b) throws IOException {
        byteBuffer.put((byte)b);
        if (!byteBuffer.hasRemaining()) doFlush();
        ioop.perform();
    }

    @Override
    public void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {

        if (byteBuffer.remaining() < len) {
            byteBuffer.put(b, off, len);
            ioop.perform();
            return;
        }

        doFlush();

        if (byteBuffer.remaining() < len) {
            byteBuffer.put(b, off, len);
            ioop.perform();
            return;
        }

        int pos = off;

        while (pos < b.length) {
            final int toWrite = min(byteBuffer.remaining(), b.length - pos);
            byteBuffer.put(b, pos, toWrite);
            if (!byteBuffer.hasRemaining()) doFlush();
            pos += toWrite;
        }

        ioop.perform();

    }

    @Override
    public void flush() throws IOException {
        doFlush();
    }

    private void doFlush() throws IOException {
        byteBuffer.flip();
        wbc.write(byteBuffer);
        byteBuffer.clear();
    }

    @Override
    public void close() throws IOException {
        doFlush();
        close.perform();
    }

    @FunctionalInterface
    private interface Operation {
        void perform() throws IOException;
        Operation NOOP = () -> {};
    }

}
