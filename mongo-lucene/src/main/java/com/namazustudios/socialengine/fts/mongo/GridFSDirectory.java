package com.namazustudios.socialengine.fts.mongo;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.apache.lucene.store.*;
import org.bson.Document;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

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

        return Lists.transform(indexGridFSbucket.find(new BasicDBObject()), new Function<GridFSDBFile, String>() {
            @Override
            public String apply(GridFSDBFile input) {
                return input.getFilename();
            }
        }).toArray(new String[]{});

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
    public IndexOutput createOutput(String name, IOContext context) throws IOException {

        checkOpen();

        final GridFSInputFile gridFSInputFile = indexGridFSbucket.createFile(name);

        return new OutputStreamIndexOutput(
                gridFSInputFile.toString(),
                gridFSInputFile.getOutputStream(),
                bufferSize);

    }

    @Override
    public void sync(Collection<String> names) throws IOException {

        checkOpen();

        // The javadoc for the directory class says this can be a no-op, and
        // that's exactly what this is.

    }

    @Override
    public void renameFile(String source, String dest) throws IOException {

        checkOpen();

        final GridFSDBFile file = indexGridFSbucket.findOne(source);

        if (file == null) {
            throw new FileNotFoundException(source + " not found.");
        }

        file.put("filename", dest);
        file.save();

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
