package com.namazustudios.socialengine.fts.mongo;

import com.google.common.io.ByteStreams;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSUploadStream;
import org.apache.lucene.store.BaseDirectoryTestCase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.TestRuleLimitSysouts;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by patricktwohig on 5/18/15.
 */
@TestRuleLimitSysouts.Limit(bytes = 512 * 1024)
public class GridFSDirectoryTest extends BaseDirectoryTestCase {

    public static final String MONGO_LOCK_COLLECTION_NAME = "locks";

    public static final String MONGO_DIRECTORY_BUCKET_NAME = "index";

    private static EmbeddedMongo embeddedMongo;

    @BeforeClass
    public static void setUpMongo() throws Exception {
        embeddedMongo = new EmbeddedMongo();
    }

    @AfterClass
    public static void tearDownMongo() throws Exception {
        embeddedMongo.close();
    }

    @Override
    protected Directory getDirectory(final Path path) throws IOException {
        final MongoDatabase mongoDatabase = embeddedMongo.getMongoDatabase();
        final MongoCollection<Document> lockCollection = mongoDatabase.getCollection(MONGO_LOCK_COLLECTION_NAME);
        final MongoLockFactory mongoLockFactory = new MongoLockFactory(lockCollection);
        final String bucketName = String.format("%s.%s", MONGO_DIRECTORY_BUCKET_NAME, path.getFileName());
        final GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDatabase, bucketName);
        return new GridFSDirectory(mongoLockFactory, gridFSBucket);
    }

    public void testReadWriteFile() throws Exception {
        final Random random = new Random();
        final byte[] array = new byte[1024 * 128];
        random.nextBytes(array);

        final MongoDatabase mongoDatabase = embeddedMongo.getMongoDatabase();
        final GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDatabase, "mytestbucket");

        try (final GridFSUploadStream os = gridFSBucket.openUploadStream("mytestfile")) {
            os.write(array);
        }

        final Set<Thread> threadSet = new HashSet<>();
        final ConcurrentMap<Integer, byte[]> results = new ConcurrentHashMap<>();

        for (int i = 0; i < 10; ++i) {
            final int threadNumber = i;
            threadSet.add(new Thread(() -> {
                try (final GridFSDownloadStream is = gridFSBucket.openDownloadStream("mytestfile")) {
                    final byte[] output = new byte[1024 * 128];
                    ByteStreams.readFully(is, output);
                    results.put(threadNumber, output);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }));
        }

        threadSet.forEach(thread -> thread.start());
        threadSet.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        results.forEach((k,v) -> assertTrue("Thread result " + k, Arrays.equals(array, v)));

    }

    @Override
    public void testFsyncDoesntCreateNewFiles() throws Exception {}

    @Override
    public void testPendingDeletions() throws IOException {}

}
