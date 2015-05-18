package com.namazustudios.socialengine.dao.mongo.guice;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.gridfs.GridFS;
import com.namazustudios.socialengine.dao.mongo.provider.MongoDatabaseProvider;
import com.namazustudios.socialengine.fts.mongo.GridFSDirectory;
import org.apache.lucene.store.Directory;
import org.bson.Document;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 5/17/15.
 */
public class MongoDirectoryProvider implements Provider<Directory> {

    public static final String SEARCH_INDEX_BUCKET = "com.namazustudios.socialengine.mongo.search.index.bucket";

    public static final String LOCK_COLLECTION = "com.namazustudios.socialengine.mongo.search.index.lock.collection";

    @Inject
    @Named(SEARCH_INDEX_BUCKET)
    private String searchIndexBucketName;

    @Inject
    @Named(LOCK_COLLECTION)
    private String lockCollectionName;

    @Inject
    @Named(MongoDatabaseProvider.DATABASE_NAME)
    private String mongoDatabaseName;

    @Inject
    private Provider<MongoClient> mongoClientProvider;

    @Override
    public Directory get() {
        final MongoClient mongoClient = mongoClientProvider.get();
        final GridFS gridFS = new GridFS(mongoClient.getDB(mongoDatabaseName), searchIndexBucketName);
        final MongoDatabase mongoDatabase = mongoClient.getDatabase(mongoDatabaseName);
        final MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(lockCollectionName);
        return new GridFSDirectory(mongoCollection, gridFS);
    }

}
