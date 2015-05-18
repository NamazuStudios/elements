package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.gridfs.GridFSDBFile;
import org.apache.lucene.store.IndexInput;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by patricktwohig on 5/17/15.
 */
public class GridFSDBFileIndexInput extends IndexInput {

    // Immutable members

    private final GridFSDBFile gridFSDBFile;

    private final long length;

    private final long sliceBegin;

    // Mutable members

    private long absolutePosition;

    private InputStream inputStream;

    public GridFSDBFileIndexInput(final GridFSDBFile gridFSDBFile) throws IOException {
        this(gridFSDBFile, 0, gridFSDBFile.getLength());
    }

    public GridFSDBFileIndexInput(final GridFSDBFile gridFSDBFile,
                                  final long sliceBegin,
                                  final long length) throws IOException {
        super(gridFSDBFile.toString());

        if ((sliceBegin + length) > gridFSDBFile.getLength()) {
            throw new IllegalArgumentException("requested range exceeds file size");
        }

        this.gridFSDBFile = gridFSDBFile;
        this.sliceBegin = sliceBegin;
        this.length = length;
        this.inputStream = gridFSDBFile.getInputStream();

        if (sliceBegin > 0) {
            absolutePosition = this.inputStream.skip(sliceBegin);
        }

    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public long getFilePointer() {
        return absolutePosition - sliceBegin;
    }

    @Override
    public void seek(final long pos) throws IOException {

        if (pos < 0 || pos >= length) {
            throw new IllegalArgumentException("absolutePosition must be >0 and < length");
        }

        final long desiredAbsolutePosition = sliceBegin + pos;

        if (this.absolutePosition < desiredAbsolutePosition) {

            // If we're moving forward, then we must skip to a specific point
            // in the file.  When we're done, we want to make sure we properly
            // record the position so it doesn't get out of sync.

            final long toSkip = desiredAbsolutePosition - this.absolutePosition;
            this.absolutePosition += inputStream.skip(toSkip);

        } else {

            // We're skipping backwards, this means that we need to actually
            // open the file again, and just skip to that position.  Of course,
            // we have to keep track of the position just like above.

            inputStream.close();
            inputStream = gridFSDBFile.getInputStream();
            this.absolutePosition = inputStream.skip(desiredAbsolutePosition);

        }

    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public IndexInput slice(String sliceDescription, long offset, long length) throws IOException {
        return new GridFSDBFileIndexInput(gridFSDBFile, sliceBegin + offset, length);
    }

    @Override
    public byte readByte() throws IOException {
        return (byte)inputStream.read();
    }

    @Override
    public void readBytes(byte[] b, int offset, int len) throws IOException {
        absolutePosition += inputStream.read(b, offset, len);
    }

}
