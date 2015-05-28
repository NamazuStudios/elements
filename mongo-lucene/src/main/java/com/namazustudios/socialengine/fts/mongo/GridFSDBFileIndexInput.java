package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.gridfs.GridFSDBFile;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by patricktwohig on 5/21/15.
 */
public class GridFSDBFileIndexInput extends IndexInput {

    // Immutable members

    private final Object lock;

    private final GridFSDBFile gridFSDBFile;

    private final long begin;

    private final long length;

    // Mutable members

    private long pos = 0;  // position relative to beginning of the slice

    private boolean open = true; // set to true until close is called, at which point all methods should throw

    private InputStream inputStream;

    public GridFSDBFileIndexInput(final GridFSDBFile gridFSDBFile) throws IOException  {
        this(gridFSDBFile.toString(), gridFSDBFile);
    }

    public GridFSDBFileIndexInput(final String resourceDescription, final GridFSDBFile gridFSDBFile)  throws IOException {
        this(resourceDescription, gridFSDBFile, 0, gridFSDBFile.getLength());
    }

    public GridFSDBFileIndexInput(final String resourceDescription, final GridFSDBFile gridFSDBFile,
                                       final long begin, final long length)  throws IOException {
        this(new Object(), resourceDescription, gridFSDBFile, begin, length);
    }

    private GridFSDBFileIndexInput(final Object lock, final String resourceDescription, final GridFSDBFile gridFSDBFile,
                                  final long begin, final long length) throws IOException {

        super(resourceDescription);

        if (begin < 0 || length < 0) {
            throw new IllegalArgumentException("begin or length cannot be less than zero");
        } else if ((begin + length) > gridFSDBFile.getLength()) {
            throw new IllegalArgumentException("length exceeds length of file");
        }

        this.lock = lock;
        this.begin = begin;
        this.length = length;
        this.gridFSDBFile = gridFSDBFile;
        this.inputStream = gridFSDBFile.getInputStream();
        skipNBytes(begin);

    }

    @Override
    public void close() throws IOException {
        synchronized (lock) {
            open = false;
        }
    }

    @Override
    public long getFilePointer() {
        synchronized (lock) {
            return pos;
        }
    }

    @Override
    public void seek(final long pos) throws IOException {
        synchronized (lock) {

            checkOpen();

            if (pos < 0) {
                throw new IllegalArgumentException("position must be positive");
            } else if (this.pos != pos) {

                // Close an re-open the stream so we start back at the
                // beginning of the file

                inputStream.close();
                inputStream = gridFSDBFile.getInputStream();

                // Skip to the absolute offset which is calculated as the
                // beginning of the slice plus the position.  Mongo's documentation
                // indicates that the mere skipping of bytes is a cheap operation
                // and doesn't actually read anything form the database.

                skipNBytes(begin + pos);
                this.pos = pos;

            }

        }
    }

    // Supports the seek function
    private long skipNBytes(final long toSkip) throws IOException {

        long total = 0;
        long remaining = toSkip;

        if (toSkip > gridFSDBFile.getLength()) {
            // This will loop infinitely if we don't throw in a check here.
            throw new EOFException("attempted to seek past end of file");
        }

        do {

            final long skipped = inputStream.skip(toSkip);

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
        synchronized (lock) {
            return length;
        }
    }

    @Override
    public GridFSDBFileIndexInput slice(String sliceDescription, final long offset, final long length) throws IOException {
        synchronized (lock) {

//            if ((begin + offset + length) > gridFSDBFile.getLength()) {
//                throw new IllegalArgumentException("exceeds length of this slice " + length());
//            }

            return new GridFSDBFileIndexInput(sliceDescription, gridFSDBFile, begin + offset, length);

        }
    }

    @Override
    public byte readByte() throws IOException {

        // Not the best way to do this, but this is just a test for now to see if it works
        // we'll add some automatic stream seeking later.  It should be noted that the
        // actual skip operation on the GridFS implementation seems to actually be
        // pretty light weight.

        synchronized (lock) {

            final int value = inputStream.read();

            if (value < 0) {
                throw new EOFException("reached end of stream");
            }

            ++pos;
            return (byte) value;

        }

    }

    @Override
    public void readBytes(final byte[] b, int offset, int length) throws IOException {

        synchronized (lock) {

            // From what I can tell, the reference implementation does not enforce
            // an EOF in this method.  Rather, this will just read zero bytes.

            final long absolute = Math.min(begin + pos, length());

            // Correct the length of the read in case there are fewer bytes
            // available.  If the absolute position ends up to be the end of
            // the file, then this method call becomes a no-op

            length = (int) Math.max(length, absolute - length());

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

    }

    protected void checkOpen() throws IOException {
        synchronized (lock) {
            if (!open) {
                throw new AlreadyClosedException(toString() + " already closed");
            }
        }
    }

    @Override
    public GridFSDBFileIndexInput clone() {

        final GridFSDBFileIndexInput cloneOfthis;

        // This one inherits the lock from the outer file.

        try {
            cloneOfthis = new GridFSDBFileIndexInput(lock, toString(), gridFSDBFile, begin, length) {

                @Override
                protected void checkOpen() throws IOException {

                    // We disregard this' open flag and delegate to the owning
                    // instance's flag, not this object.

                    GridFSDBFileIndexInput.this.checkOpen();

                }

                @Override
                public void close() throws IOException {

                    // The API says it will never close the cloned instances of the
                    // object and that it still has to signal properly with an
                    // AlreadyClosedException.  However, if this should succeed
                    // we simply want to throw an exception indicating that this
                    // operation is not supported.

                    throw new UnsupportedOperationException("close must not be called on clone");

                }

            };
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return cloneOfthis;

    }

}
