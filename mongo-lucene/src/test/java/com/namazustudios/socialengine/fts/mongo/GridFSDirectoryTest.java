package com.namazustudios.socialengine.fts.mongo;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.gridfs.GridFS;
import org.apache.lucene.store.BaseDirectoryTestCase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Created by patricktwohig on 5/18/15.
 */
public class GridFSDirectoryTest extends BaseDirectoryTestCase {

    public static final int DEFAULT_MONGO_PORT = 27017;

    public static final String MONGO_SCHEME = "mongo";

    public static final String MONGO_DB_URLS = "com.namazustudios.socialengine.fts.test.mongo.db.url";

    public static final String MONGO_DB_NAME = "com.namazustudios.socialengine.fts.test.mongo.db.name";

    public static final String MONGO_LOCK_COLLECTION_NAME =
            "com.namazustudios.socialengine.fts.test.mongo.lock.collection.name";

    public static final String MONGO_DIRECTORY_BUCKET_NAME =
            "com.namazustudios.socialengine.fts.test.mongo.directory.bucket.name";

    private static MongoClient mongoClient;

    private static MongoDatabase mongoDatabase;

    @Before
    public void setUpMongo() {
        mongoClient = getMongoClient();
        mongoDatabase = getMongoDatabase();
    }

    @After
    public void tearDownMongo() {
        mongoDatabase.dropDatabase();
        mongoClient.close();
    }

    @Override
    protected Directory getDirectory(Path path) throws IOException {
        return new GridFSDirectory(new SingleInstanceLockFactory(), getDirectoryBucket());
    }

    private static MongoLockFactory getMongoLockFactory() {
        final String lockCollectionName = System.getProperties().getProperty(MONGO_LOCK_COLLECTION_NAME, "fts-locks");
        final MongoCollection<Document> lockCollection = mongoDatabase.getCollection(lockCollectionName);
        return new MongoLockFactory(lockCollection);
    }

    private static GridFS getDirectoryBucket() {
        final String bucketName = System.getProperties().getProperty(MONGO_DIRECTORY_BUCKET_NAME, "fts-index");
        return new GridFS(getDB(), bucketName);
    }

    private static DB getDB() {
        final String databaseName = System.getProperties().getProperty(MONGO_DB_NAME, "test-database");
        final DB db = mongoClient.getDB(databaseName);
        db.setWriteConcern(WriteConcern.ACKNOWLEDGED);
        return db;
    }

    private static MongoDatabase getMongoDatabase() {
        final String databaseName = System.getProperties().getProperty(MONGO_DB_NAME, "test-database");
        return mongoClient.getDatabase(databaseName);
    }

    @Override
    public void testFsyncDoesntCreateNewFiles() throws Exception {}

    private static MongoClient getMongoClient() {

        final String mongoDbUrls = System.getProperties().getProperty(MONGO_DB_URLS, "mongo://localhost");

        final List<ServerAddress> serverAddressList = Lists.transform(Arrays.asList(mongoDbUrls.split(",")),
                new Function<String, ServerAddress>() {
                    @Override
                    public ServerAddress apply(String input) {

                        final URI uri;

                        try {
                            uri = new URI(input);
                        } catch (URISyntaxException ex) {
                            throw new IllegalArgumentException("Invalid URI", ex);
                        }

                        if (uri.getScheme() != null && !MONGO_SCHEME.equals(uri.getScheme())) {
                            throw new IllegalArgumentException("Invalid scheme" + uri.getScheme());
                        }

                        final String host = uri.getHost();
                        final int port = uri.getPort() < 0 ? DEFAULT_MONGO_PORT : uri.getPort();

                        return new ServerAddress(host, port);

                    }
                });

        return new MongoClient(serverAddressList);

    }

}
