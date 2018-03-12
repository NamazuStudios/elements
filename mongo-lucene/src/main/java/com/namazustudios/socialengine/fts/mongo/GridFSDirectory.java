package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.apache.lucene.store.*;
import org.bson.Document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;
import static java.lang.String.format;
import static java.util.Comparator.naturalOrder;
import static java.util.UUID.randomUUID;

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

    private final GridFSBucket indexGridFSbucket;

    public GridFSDirectory(final MongoCollection<Document> lockCollection,
                           final GridFSBucket indexGridFSbucket) {
        this(new MongoLockFactory(lockCollection), indexGridFSbucket);
    }

    public GridFSDirectory(final LockFactory lockFactory,
                           final GridFSBucket indexGridFSbucket) {
        this(lockFactory, indexGridFSbucket, DEFAULT_BUFFER_SIZE);
    }

    public GridFSDirectory(final LockFactory lockFactory,
                           final GridFSBucket indexGridFSbucket,
                           final int bufferSize) {

        super(lockFactory);

        this.bufferSize = bufferSize;
        this.indexGridFSbucket = indexGridFSbucket;

    }

    @Override
    public String[] listAll() throws IOException {
        checkOpen();
        final ArrayList<String> results = new ArrayList<>();
        indexGridFSbucket.find().forEach((Consumer<? super GridFSFile>) file -> results.add(file.getFilename()));
        results.sort(naturalOrder());
        return results.toArray(new String[results.size()]);
    }

    @Override
    public void deleteFile(final String name) throws IOException {
        checkOpen();
        final GridFSFile file = indexGridFSbucket.find(eq("filename", name)).first();
        indexGridFSbucket.delete(file.getId());
    }

    @Override
    public long fileLength(final String name) throws IOException {

        checkOpen();

        final GridFSFile file = indexGridFSbucket.find(eq("filename", name)).first();

        if (file == null) {
            throw new FileNotFoundException();
        }

        return file.getLength();

    }

    @Override
    public IndexOutput createOutput(final String name, final IOContext context) throws IOException {
        return createOutput(name, false);
    }

    @Override
    public IndexOutput createTempOutput(final String prefix, final String suffix, final IOContext context) throws IOException {
        final String filename = format("%s%s%s.tmp", prefix, randomUUID(), suffix);
        return createOutput(filename, true);
    }

    public IndexOutput createOutput(final String name, final boolean temporary) throws IOException {
        checkOpen();

        final Document metadata = new Document();
        metadata.put("temporary", temporary);

        final GridFSUploadStream uploadStream;

        uploadStream = indexGridFSbucket.openUploadStream(name, new GridFSUploadOptions()
            .metadata(metadata)
            .chunkSizeBytes(bufferSize));

        final String resourceDescription = format("gridfs://%s/%s - (%s)", indexGridFSbucket.getBucketName(), name, uploadStream.getId());
        return new OutputStreamIndexOutput(resourceDescription, name, uploadStream, bufferSize);

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
        final GridFSFile file = indexGridFSbucket.find(eq("filename", source)).first();
        indexGridFSbucket.rename(file.getId(), dest);
    }

    @Override
    public IndexInput openInput(final String name, final IOContext context) throws IOException {

        checkOpen();

        final GridFSFile file = indexGridFSbucket.find(eq("filename", name)).first();

        if (file == null) {
            throw new FileNotFoundException(name + " not found.");
        }

        return new GridFSDBFileIndexInput("gridfs://" + name, indexGridFSbucket, file);

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

    public GridFSBucket getIndexGridFSbucket() {
        return indexGridFSbucket;
    }

    @Override
    public String toString() {
        return "GridFSDirectory{" +
                "open=" + open +
                ", indexGridFSbucket=" + indexGridFSbucket +
                '}';
    }

}
