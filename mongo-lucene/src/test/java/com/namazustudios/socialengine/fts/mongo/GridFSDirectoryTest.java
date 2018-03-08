package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.apache.lucene.store.BaseDirectoryTestCase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by patricktwohig on 5/18/15.
 */
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
        return new GridFSDirectory(new SingleInstanceLockFactory(), gridFSBucket);
    }

    @Override
    public void testFsyncDoesntCreateNewFiles() throws Exception {}

    @Override
    public void testPendingDeletions() throws IOException {}

}
