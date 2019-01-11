package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.gridfs.GridFS;
import com.namazustudios.elements.fts.mongo.GridFSDirectory;
import com.namazustudios.elements.fts.mongo.MongoLockFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SleepingLockWrapper;
import org.bson.Document;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static com.namazustudios.socialengine.dao.mongo.provider.MongoDatabaseProvider.*;

/**
 * Created by patricktwohig on 5/17/15.
 */
public class MongoDirectoryProvider implements Provider<Directory> {

    public static final String SEARCH_INDEX_BUCKET = "com.namazustudios.socialengine.mongo.search.index.bucket";

    public static final String LOCK_COLLECTION = "com.namazustudios.socialengine.mongo.search.index.lock.collection";

    private String searchIndexBucketName;

    private String lockCollectionName;

    private String mongoDatabaseName;

    private Provider<MongoClient> mongoClientProvider;

    @Override
    public Directory get() {
        final MongoClient mongoClient = getMongoClientProvider().get();
        final MongoDatabase mongoDatabase = mongoClient.getDatabase(getMongoDatabaseName());
        final GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDatabase, getSearchIndexBucketName());
        final MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(getLockCollectionName());
        //return new SleepingLockWrapper(new GridFSDirectory(mongoCollection, gridFSBucket, 0), 250);
        return new SleepingLockWrapper(new GridFSDirectory(new MongoLockFactory(null, null, null, 0, 0, 0, 0, 0, 0), gridFSBucket, 0), 250);
    }

    public String getSearchIndexBucketName() {
        return searchIndexBucketName;
    }

    @Inject
    public void setSearchIndexBucketName(@Named(SEARCH_INDEX_BUCKET) String searchIndexBucketName) {
        this.searchIndexBucketName = searchIndexBucketName;
    }

    public String getLockCollectionName() {
        return lockCollectionName;
    }

    @Inject
    public void setLockCollectionName(@Named(LOCK_COLLECTION) String lockCollectionName) {
        this.lockCollectionName = lockCollectionName;
    }

    public String getMongoDatabaseName() {
        return mongoDatabaseName;
    }

    @Inject
    public void setMongoDatabaseName(@Named(DATABASE_NAME) String mongoDatabaseName) {
        this.mongoDatabaseName = mongoDatabaseName;
    }

    public Provider<MongoClient> getMongoClientProvider() {
        return mongoClientProvider;
    }

    @Inject
    public void setMongoClientProvider(Provider<MongoClient> mongoClientProvider) {
        this.mongoClientProvider = mongoClientProvider;
    }

}
