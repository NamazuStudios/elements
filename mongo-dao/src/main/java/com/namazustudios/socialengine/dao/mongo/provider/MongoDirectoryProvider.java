package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.gridfs.GridFS;
import com.namazustudios.elements.fts.mongo.GridFSDirectory;
import com.namazustudios.elements.fts.mongo.GridFSDirectoryBuilder;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockFactory;
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

    private String mongoDatabaseName;

    private String searchIndexBucketName;

    private Provider<MongoClient> mongoClientProvider;

    private Provider<LockFactory> lockFactoryProvider;

    @Override
    public Directory get() {
        final LockFactory lockFactory = getLockFactoryProvider().get();
        final MongoClient mongoClient = getMongoClientProvider().get();
        final MongoDatabase mongoDatabase = mongoClient.getDatabase(getMongoDatabaseName());
        final GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDatabase, getSearchIndexBucketName());
        return new GridFSDirectoryBuilder().build(lockFactory, gridFSBucket);
    }

    public String getMongoDatabaseName() {
        return mongoDatabaseName;
    }

    @Inject
    public void setMongoDatabaseName(@Named(DATABASE_NAME) String mongoDatabaseName) {
        this.mongoDatabaseName = mongoDatabaseName;
    }

    public String getSearchIndexBucketName() {
        return searchIndexBucketName;
    }

    @Inject
    public void setSearchIndexBucketName(@Named(SEARCH_INDEX_BUCKET) String searchIndexBucketName) {
        this.searchIndexBucketName = searchIndexBucketName;
    }

    public Provider<MongoClient> getMongoClientProvider() {
        return mongoClientProvider;
    }

    @Inject
    public void setMongoClientProvider(Provider<MongoClient> mongoClientProvider) {
        this.mongoClientProvider = mongoClientProvider;
    }

    public Provider<LockFactory> getLockFactoryProvider() {
        return lockFactoryProvider;
    }

    @Inject
    public void setLockFactoryProvider(Provider<LockFactory> lockFactoryProvider) {
        this.lockFactoryProvider = lockFactoryProvider;
    }

}
