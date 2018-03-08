package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.lucene.store.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by patricktwohig on 5/21/15.
 */
public class GridFSDBFileIndexInput extends BufferedIndexInput {

    private static final Logger logger = LoggerFactory.getLogger(GridFSDBFileIndexInput.class);

    private long pos = 0;

    private AtomicBoolean open = new AtomicBoolean(true);

    private GridFSBucket gridFSBucket;

    private String filename;

    public GridFSDBFileIndexInput(final String resourceDesc,
                                  final IOContext context,
                                  final GridFSBucket gridFSBucket,
                                  final String filename) {
        super(resourceDesc, context);
        this.gridFSBucket = gridFSBucket;
        this.filename = filename;
    }

    private void checkOpenAndThrowIfNecessary() {
        if (!open.get()) {
            throw new AlreadyClosedException(this + " is already closed.");
        }
    }

    @Override
    protected void readInternal(byte[] b, int offset, int length) throws IOException {

        checkOpenAndThrowIfNecessary();

        if (pos < 0) {
            return;
        }

        try (final InputStream is = gridFSBucket.openDownloadStream(filename)) {
            is.skip(pos);

            int total = is.read(b, offset, length);

            while (total < length) {

                final int read = is.read(b, offset + total, length - total);

                if (read < 0) {
                    pos = -1;
                    return;
                }

            }

            pos += total;

        }

    }

    @Override
    protected void seekInternal(long pos) throws IOException {
        checkOpenAndThrowIfNecessary();

        if (pos > length()) {
            throw new EOFException();
        }

        this.pos = pos;

    }

    @Override
    public void close() throws IOException {
        if (!open.compareAndSet(true, false)) {
            logger.warn("Already closed file {}" + this);
        }
    }

    @Override
    public GridFSDBFileIndexInput clone() {
        final GridFSDBFileIndexInput clone = (GridFSDBFileIndexInput)super.clone();
        clone.pos = pos;
        clone.open = open;
        clone.filename = filename;
        return clone;
    }

    @Override
    public long length() {
        final GridFSFile file = gridFSBucket.find(eq("filename", filename)).first();
        return file.getLength();
    }

}
