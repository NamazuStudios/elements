package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.gridfs.GridFSDBFile;
import org.apache.lucene.store.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by patricktwohig on 5/17/15.
 */
public class GridFSDBFileBufferedIndexInput extends BufferedIndexInput {

    // Immutable members

    private Object lock = new Object();

    private final GridFSDBFile gridFSDBFile;

    // Mutable members

    private long pos = 0;

    private boolean open = true;

    public GridFSDBFileBufferedIndexInput(final GridFSDBFile gridFSDBFile) {
        super(gridFSDBFile.toString());
        this.gridFSDBFile = gridFSDBFile;
    }

    public GridFSDBFileBufferedIndexInput(final String resourceDesc,
                                          final GridFSDBFile gridFSDBFile) {
        super(resourceDesc);
        FSDirectory fsd;
        RAMDirectory rd;
        this.gridFSDBFile = gridFSDBFile;
    }

    public GridFSDBFileBufferedIndexInput(final String resourceDesc,
                                          final IOContext context,
                                          final GridFSDBFile gridFSDBFile) {
        super(resourceDesc, context);
        this.gridFSDBFile = gridFSDBFile;
    }

    public GridFSDBFileBufferedIndexInput(final String resourceDesc,
                                          final int bufferSize,
                                          final GridFSDBFile gridFSDBFile) {
        super(resourceDesc, bufferSize);
        this.gridFSDBFile = gridFSDBFile;
    }

    @Override
    protected void readInternal(byte[] b, int offset, int length) throws IOException {

        synchronized (lock) {

            checkOpen();

            if (pos >= gridFSDBFile.getLength()) {
                throw new EOFException("attempting to read past end of file.");
            }

            try (final InputStream inputStream = gridFSDBFile.getInputStream()) {

                skipNBytes(inputStream, pos);

                do {

                    final int read = inputStream.read(b, offset, length);

                    offset += read;
                    length -= read;

                } while (length > 0);

                if (length < 0) {
                    throw new IOException("read more bytes than expected");
                }

                pos += length;

            }

        }

    }

    private void skipNBytes(final InputStream inputStream, final long toSkip) throws IOException {

        long total = 0;
        long remaining = toSkip;

        do {

            final long skipped = inputStream.skip(toSkip);

            total += skipped;
            remaining -= skipped;

        } while(remaining > 0);

        if (remaining < 0) {
            throw new IOException("skipped more bytes than expected");
        }

        pos += total;

    }

    @Override
    protected void seekInternal(final long pos) throws IOException {

        synchronized (lock) {

            checkOpen();

            if (pos > gridFSDBFile.getLength()) {
                throw new EOFException("attempted to seek past end of file");
            } else if (pos < 0) {
                throw new IllegalArgumentException("position must be positive");
            }

            this.pos = pos;

        }

    }

    @Override
    public void close() throws IOException {
        synchronized (lock) {
            checkOpen();
            open = false;
        }
    }

    protected void checkOpen() throws IOException {
        if (!open) {
            throw new AlreadyClosedException("input not open");
        }
    }

    @Override
    public long length() {
        synchronized (lock) {
            return gridFSDBFile.getLength();
        }
    }

    @Override
    public BufferedIndexInput clone() {

        final GridFSDBFileBufferedIndexInput cloneOfThis =
            new GridFSDBFileBufferedIndexInput(toString(), getBufferSize(), gridFSDBFile) {

                @Override
                protected void checkOpen() throws IOException {
                    GridFSDBFileBufferedIndexInput.this.checkOpen();
                }

                @Override
                public void close() throws IOException {
                    throw new UnsupportedOperationException("close must not be called on clone");
                }

            };

        cloneOfThis.lock = lock;
        cloneOfThis.pos = pos;

        return cloneOfThis;

    }

}
