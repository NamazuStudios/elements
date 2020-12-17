package com.namazustudios.socialengine.dao.mongo.provider;

import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.namazustudios.socialengine.dao.mongo.MongoGridFSFileDao;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static com.namazustudios.socialengine.dao.mongo.provider.MongoDatabaseProvider.DATABASE_NAME;

/**
 * Created by patricktwohig on 6/29/17.
 */
public class MongoGridFSFileDaoProvider implements Provider<MongoGridFSFileDao> {

    public static final String FILE_SERVICE_BUCKET = "com.namazustudios.socialengine.mongo.file.service.bucket";

    private String mongoDatabaseName;

    private String mongoFileBucketName;

    private Provider<MongoClient> mongoClientProvider;

    @Override
    public MongoGridFSFileDao get() {
        final MongoClient mongoClient = getMongoClientProvider().get();
        final GridFS gridFS = new GridFS(mongoClient.getDB(getMongoDatabaseName()), getMongoFileBucketName());
        final MongoGridFSFileDao mongoGridFSFileDao = new MongoGridFSFileDao();
        mongoGridFSFileDao.setGridFS(gridFS);
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
