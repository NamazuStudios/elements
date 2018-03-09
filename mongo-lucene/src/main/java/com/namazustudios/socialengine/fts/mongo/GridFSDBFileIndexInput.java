package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.IndexInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;

/**
 * Created by patricktwohig on 5/21/15.
 */
public class GridFSDBFileIndexInput extends IndexInput {

    private static final Logger logger = LoggerFactory.getLogger(GridFSDBFileIndexInput.class);

    // Immutable members

    private final AtomicBoolean open = new AtomicBoolean(true);

    private final long begin;

    private final long length;

    private final GridFSBucket gridFSBucket;

    private final GridFSFile gridFSFile;

    // Mutable members

    private long pos;  // position relative to beginning of the slice

    private GridFSDownloadStream inputStream;

    public GridFSDBFileIndexInput(final String resourceDescription,
                                  final GridFSBucket gridFSBucket,
                                  final GridFSFile gridFSFile)  throws IOException {
        this(resourceDescription, gridFSBucket, gridFSFile, 0, gridFSFile.getLength());
    }

    private GridFSDBFileIndexInput(final String resourceDescription,
                                   final GridFSBucket gridFSBucket,
                                   final GridFSFile gridFSFile,
                                   final long begin, final long length)  throws IOException {
        this(resourceDescription, gridFSBucket, gridFSFile, 0, begin, length);
    }

    private GridFSDBFileIndexInput(final String resourceDescription,
                                   final GridFSBucket gridFSBucket,
                                   final GridFSFile gridFSFile,
                                   final long pos, final long begin, final long length) throws IOException {

        super(resourceDescription);

        if (begin < 0 || length < 0) {
            throw new IllegalArgumentException("begin or length cannot be less than zero");
        } else if ((begin + length) > gridFSFile.getLength()) {
            throw new IllegalArgumentException("length exceeds length of file");
        } else if (pos < 0) {
            throw new IllegalArgumentException("position must be positive");
        } else if (pos > length) {
            throw new IllegalArgumentException("position must not be greater than length");
        }

        this.pos = pos;
        this.begin = begin;
        this.length = length;
        this.gridFSFile = gridFSFile;
        this.gridFSBucket = gridFSBucket;
        this.inputStream = gridFSBucket.openDownloadStream(gridFSFile.getId());
        skipNBytes(begin + pos);

    }

    @Override
    public void close() throws IOException {
        if (!open.compareAndSet(true, false)) {
            logger.warn("Index input already closed.");
        }
    }

    @Override
    public long getFilePointer() {
        return pos;
    }

    @Override
    public void seek(final long pos) throws IOException {

        checkOpen();

        if (pos < 0) {
            throw new IllegalArgumentException("position must be positive");
        } else if (this.pos != pos) {

            // Close an re-open the stream so we start back at the
            // beginning of the file

            inputStream.close();
            inputStream = gridFSBucket.openDownloadStream(gridFSFile.getId());

            // Skip to the absolute offset which is calculated as the
            // beginning of the slice plus the position.  Mongo's documentation
            // indicates that the mere skipping of bytes is a cheap operation
            // and doesn't actually read anything form the database.

            skipNBytes(begin + pos);
            this.pos = pos;

        }

    }

    // Supports the seek function
    private long skipNBytes(final long toSkip) throws IOException {

        long total = 0;
        long remaining = toSkip;

        if (toSkip > gridFSFile.getLength()) {
            // This will loop infinitely if we don't throw in a check here.
            final String msg = format("attempted to seek past end of file toSkip: %d limit: %d", toSkip, gridFSFile.getLength());
            throw new EOFException(msg);
        }

        do {

            final long skipped = inputStream.skip(remaining);

            total += skipped;
            remaining -= skipped;

        } while(remaining > 0);

        if (remaining < 0) {
            // Should never happen, but if it does then I want o at least
            // flag this situation.
            throw new IOException("skipped more bytes than expected");
        }

        return total;

    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public GridFSDBFileIndexInput slice(String sliceDescription, final long offset, final long length) throws IOException {

        if ((offset + length) > length()) {
            throw new IllegalArgumentException("exceeds length of this slice " + length());
        }

        return new GridFSDBFileIndexInput(sliceDescription, gridFSBucket, gridFSFile, begin + offset, length);

    }

    @Override
    public byte readByte() throws IOException {

        // Not the best way to do this, but this is just a test for now to see if it works
        // we'll add some automatic stream seeking later.  It should be noted that the
        // actual skip operation on the GridFS implementation seems to actually be
        // pretty light weight.

        final int value = inputStream.read();

        if (value < 0) {
            throw new EOFException("reached end of stream");
        }

        ++pos;
        return (byte) value;

    }

    @Override
    public void readBytes(final byte[] b, int offset, int length) throws IOException {

        // From what I can tell, the reference implementation does not enforce
        // an EOF in this method.  Rather, this will just read zero bytes.

        length = (int)Math.min(length, length());

        int total = 0;

        while (length > 0) {

            final int read = inputStream.read(b, offset, length);

            total += read;
            offset += read;
            length -= read;

        }

        // Lastly updates the read position because we need to know
        // where we finally ended up.

        pos += total;

    }

    protected void checkOpen() throws IOException {
        if (!open.get()) {
            throw new AlreadyClosedException(toString() + " already closed");
        }
    }

    @Override
    public GridFSDBFileIndexInput clone() {

        final GridFSDBFileIndexInput clone = (GridFSDBFileIndexInput) super.clone();
        clone.inputStream = gridFSBucket.openDownloadStream(gridFSFile.getId());

        try {
            clone.skipNBytes(begin + pos);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return clone;

    }

}
