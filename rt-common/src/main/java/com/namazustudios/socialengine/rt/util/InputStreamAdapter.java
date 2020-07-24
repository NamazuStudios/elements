package com.namazustudios.socialengine.rt.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import static java.lang.Long.max;
import static java.lang.Long.min;

/**
 * Wraps an instance of {@link ReadableByteChannel} using a {@link ByteBuffer}. It is not recommended to use a
 * {@link ReadableByteChannel} that is in non-blocking mode with this particular type, as it will raise an instance of
 * {@link IOException} if the channel returns zero bytes from the channel.
 *
 * During hte process of reading, this may have read more bytes from the underlying {@link ReadableByteChannel} than
 * was consumed by the actual stream operations. However, when this {@link InputStreamAdapter} is closed, it will ensure
 * that any remaining unread bytes are preserved in the state of the backing {@link ByteBuffer}.
 */
public class InputStreamAdapter extends InputStream {

    /**
     * Indicates as to whether or not the underlying {@link ReadableByteChannel} should be closed when this is closed
     */
    public static final int CLOSE = 0x1 << 0;

    private final Operation close;
    private final ByteBuffer byteBuffer;
    private final ReadableByteChannel rbc;

    public InputStreamAdapter(final ReadableByteChannel rbc, final ByteBuffer byteBuffer, final int options) {
        this.rbc = rbc;
        this.byteBuffer = byteBuffer;
        close = (CLOSE & options) == 0 ? Operation.NOOP : rbc::close;
    }

    @Override
    public int read() throws IOException {
        if (byteBuffer.hasRemaining() || loadBuffer(false)) {
            return byteBuffer.get();
        } else {
            return -1;
        }
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (byteBuffer.hasRemaining() || loadBuffer(true)) {
            final int original = byteBuffer.position();
            byteBuffer.get(b, off, len);
            return byteBuffer.position() - original;
        } else {
            return -1;
        }
    }

    @Override
    public long skip(final long n) throws IOException {

        if (n < 0) throw new IllegalArgumentException("Invalid skip value: " + n);

        long skipped = min(n, byteBuffer.remaining());

        while ((skipped < n) && loadBuffer(true, n - skipped)) {
            skipped += byteBuffer.remaining();
        }

        return skipped;

    }

    final boolean loadBuffer(boolean allowZeroBytes) throws IOException {
        return loadBuffer(allowZeroBytes, byteBuffer.capacity());
    }

    final boolean loadBuffer(boolean allowZeroBytes, final long limitHint) throws IOException {

        byteBuffer.clear();
        byteBuffer.limit((int)min(limitHint, byteBuffer.capacity()));

        final int read = rbc.read(byteBuffer);

        if (read < 0) return false;
        else if (read == 0 && !allowZeroBytes) throw new IOException("backing channel is in nonblocking mode.");

        byteBuffer.limit(read);
        byteBuffer.rewind();

        return true;

    }

    @Override
    public void close() throws IOException {
        close.perform();
    }

    private interface Operation {
        void perform() throws IOException;
        Operation NOOP = () -> {};
    }

}
