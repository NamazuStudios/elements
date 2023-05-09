package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.client.MongoClient;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.gridfs.GridFS;
import dev.getelements.elements.dao.mongo.MongoGridFSFileDao;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static dev.getelements.elements.dao.mongo.provider.MongoDatastoreProvider.DATABASE_NAME;

/**
 * Created by patricktwohig on 6/29/17.
 */
public class MongoGridFSFileDaoProvider implements Provider<MongoGridFSFileDao> {

    public static final String FILE_SERVICE_BUCKET = "dev.getelements.elements.mongo.file.service.bucket";

    private String mongoDatabaseName;

    private String mongoFileBucketName;

    private Provider<MongoClient> mongoClientProvider;

    @Override
    public MongoGridFSFileDao get() {
        final MongoClient mongoClient = getMongoClientProvider().get();
        final GridFSBucket gridFSBucket = GridFSBuckets.create(mongoClient.getDatabase(getMongoDatabaseName()), getMongoFileBucketName());
        final MongoGridFSFileDao mongoGridFSFileDao = new MongoGridFSFileDao();
        mongoGridFSFileDao.setGridFSBucket(gridFSBucket);
        return mongoGridFSFileDao;
    }

    public String getMongoDatabaseName() {
        return mongoDatabaseName;
    }

    @Inject
    public void setMongoDatabaseName(@Named(DATABASE_NAME) String mongoDatabaseName) {
        this.mongoDatabaseName = mongoDatabaseName;
    }

    public String getMongoFileBucketName() {
        return mongoFileBucketName;
    }

    @Inject
    public void setMongoFileBucketName(@Named(FILE_SERVICE_BUCKET) String mongoFileBucketName) {
        this.mongoFileBucketName = mongoFileBucketName;
    }

    public Provider<MongoClient> getMongoClientProvider() {
        return mongoClientProvider;
    }

    @Inject
    public void setMongoClientProvider(Provider<MongoClient> mongoClientProvider) {
        this.mongoClientProvider = mongoClientProvider;
    }

}
