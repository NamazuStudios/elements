package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.apache.lucene.store.*;
import org.bson.Document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * An implementation of {@link Directory} which stores the index in a single
 * Mongo collection.  This requires the use of the {@link MongoLockFactory}
 * in order to provide index locks.
 *
 * Created by patricktwohig on 5/17/15.
 */
public class GridFSDirectory extends BaseDirectory {

    public static final int DEFAULT_BUFFER_SIZE = 4096;

    private AtomicBoolean open = new AtomicBoolean(true);

    private final int bufferSize;

    private final GridFS indexGridFSbucket;

    public GridFSDirectory(final MongoCollection<Document> lockCollection,
                           final GridFS indexGridFSbucket) {
        this(new MongoLockFactory(lockCollection), indexGridFSbucket);
    }

    public GridFSDirectory(final LockFactory lockFactory,
                           final GridFS indexGridFSbucket) {
        this(lockFactory, indexGridFSbucket, DEFAULT_BUFFER_SIZE);
    }

    public GridFSDirectory(final LockFactory lockFactory,
                           final GridFS indexGridFSbucket,
                           final int bufferSize) {

        super(lockFactory);


        this.bufferSize = bufferSize;
        this.indexGridFSbucket = indexGridFSbucket;

    }

    @Override
    public String[] listAll() throws IOException {

        checkOpen();

        return indexGridFSbucket.find(new BasicDBObject())
            .stream()
            .map( input -> input.getFilename())
            .collect(Collectors.toList())
            .toArray(new String[]{});

    }

    @Override
    public void deleteFile(String name) throws IOException {
        checkOpen();
        indexGridFSbucket.remove(name);
    }

    @Override
    public long fileLength(String name) throws IOException {

        checkOpen();

        final GridFSDBFile file = indexGridFSbucket.findOne(name);

        if (file == null) {
            throw new FileNotFoundException();
        }

        return file.getLength();

    }

    @Override
    public IndexOutput createOutput(final String name, final IOContext context) throws IOException {

        checkOpen();

        final GridFSInputFile gridFSInputFile = indexGridFSbucket.createFile(name);

        return new OutputStreamIndexOutput(
                "gridfs://" + name, name,
                gridFSInputFile.getOutputStream(),
                bufferSize);

    }

    @Override
    public IndexOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException {
        return null;
    }

    @Override
    public void sync(Collection<String> names) throws IOException {
        checkOpen();
        // The javadoc for the directory class says this can be a no-op, and that's exactly what this is.
    }

    @Override
    public void syncMetaData() throws IOException {
        checkOpen();
        // The javadoc for the directory class says this can be a no-op, and that's exactly what this is.
    }

    @Override
    public void rename(final String source, final String dest) throws IOException {

    }

    @Override
    public IndexInput openInput(String name, IOContext context) throws IOException {

        checkOpen();

        final GridFSDBFile file = indexGridFSbucket.findOne(name);

        if (file == null) {
            throw new FileNotFoundException(name + " not found.");
        }

        return new GridFSDBFileIndexInput(file);

    }

    @Override
    public void close() throws IOException {
        open.set(false);
    }

    private void checkOpen() throws IOException {
        if (!open.get()) {
            throw new AlreadyClosedException(toString() + " is closed");
        }
    }

    @Override
    public String toString() {
        return "GridFSDirectory{" +
                "open=" + open +
                ", indexGridFSbucket=" + indexGridFSbucket +
                '}';
    }

}
