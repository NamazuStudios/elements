package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.apache.lucene.store.LockObtainFailedException;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.mongodb.client.model.Filters.eq;
import static com.namazustudios.socialengine.fts.mongo.GridFSDirectoryTest.MONGO_DIRECTORY_BUCKET_NAME;
import static com.namazustudios.socialengine.fts.mongo.GridFSDirectoryTest.MONGO_LOCK_COLLECTION_NAME;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class MongoLockFactoryTest {

    private static EmbeddedMongo embeddedMongo;

    private static final long TEST_LOCK_TIMEOUT_MILLISECONDS = 1000;

    @BeforeClass
    public static void setUpMongo() throws Exception {
        embeddedMongo = new EmbeddedMongo();
    }

    @AfterClass
    public static void tearDownMongo() throws Exception {
        embeddedMongo.close();
    }

    private MongoLockFactory openMongoLockFactory() {
        final MongoDatabase mongoDatabase = embeddedMongo.getMongoDatabase();
        final MongoCollection<Document> lockCollection = mongoDatabase.getCollection(MONGO_LOCK_COLLECTION_NAME);
        return new MongoLockFactory(lockCollection, TEST_LOCK_TIMEOUT_MILLISECONDS);
    }

    private GridFSBucket openGridFSBucket() {
        final MongoDatabase mongoDatabase = embeddedMongo.getMongoDatabase();
        return GridFSBuckets.create(mongoDatabase, MONGO_DIRECTORY_BUCKET_NAME);
    }

    @Test
    public void testLockKeepAlive() throws Exception {
        try (final MongoLockFactory mongoLockFactory = openMongoLockFactory();
             final GridFSDirectory gridFSDirectory = new GridFSDirectory(mongoLockFactory, openGridFSBucket());
             final MongoLock mongoLock = mongoLockFactory.obtainLock(gridFSDirectory, "mylock")) {
            Thread.sleep(mongoLockFactory.getLockTimeoutMilliseconds() * 2);
            mongoLock.ensureValid();
        }
    }

    @Test(expected = LockExpiredException.class)
    public void testKillLockWithTimeout() throws Exception {
        try (final MongoLockFactory mongoLockFactory = openMongoLockFactory();
             final GridFSDirectory gridFSDirectory = new GridFSDirectory(mongoLockFactory, openGridFSBucket())) {
            final MongoLock mongoLock = mongoLockFactory.obtainLock(gridFSDirectory, "mylock");
            mongoLockFactory.closeNow();
            Thread.sleep(mongoLockFactory.getLockTimeoutMilliseconds() * 2);
            mongoLock.ensureValid();
        }
    }

    @Test(expected = LockObtainFailedException.class)
    public void testDoubleLock() throws Exception {
        try (final MongoLockFactory mongoLockFactory = openMongoLockFactory();
             final GridFSDirectory gridFSDirectory = new GridFSDirectory(mongoLockFactory, openGridFSBucket());
             final MongoLock mongoLock1 = mongoLockFactory.obtainLock(gridFSDirectory, "mylock");
             final MongoLock mongoLock2 = mongoLockFactory.obtainLock(gridFSDirectory, "mylock")) {}
    }

    @Test
    public void testAcquireExpiredLock() throws Exception {
        try (final MongoLockFactory mongoLockFactory1 = openMongoLockFactory();
             final MongoLockFactory mongoLockFactory2 = openMongoLockFactory();
             final GridFSDirectory gridFSDirectory = new GridFSDirectory(mongoLockFactory1, openGridFSBucket())) {

            final MongoLock mongoLock1 = mongoLockFactory1.obtainLock(gridFSDirectory, "mylock");
            mongoLockFactory1.closeNow();
            Thread.sleep(mongoLockFactory1.getLockTimeoutMilliseconds() * 2);

            try {
                mongoLock1.ensureValid();
                fail("Lock should not be vaild.");
            } catch (LockExpiredException ex) {

                final Document document = mongoLockFactory2
                    .getLockCollection()
                    .find(eq("_id", "index-mylock"))
                    .first();
                assertNotNull("Expecting expired lock to exist.", document);
                try (final MongoLock mongoLock2 = mongoLockFactory2.obtainLock(gridFSDirectory, "mylock")) {}

            }

        }
    }

}
