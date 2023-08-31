package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import dev.getelements.elements.rt.Path;
import org.bson.BsonString;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static java.util.UUID.randomUUID;

/**
 * Created by patricktwohig on 6/29/17.
 */
public class LargeObjectGridFSBucketProvider implements Provider<GridFSBucket> {

    public static final String LARGE_OBJECT_BUCKET = "dev.getelements.elements.mongo.large.object.bucket";

    private String largeObjectBucketName;

    private Provider<MongoDatabase> mongoDatabaseProvider;

    @Override
    public GridFSBucket get() {
        return GridFSBuckets.create(
                getMongoDatabaseProvider().get(),
                getLargeObjectBucketName()
        );
    }

    public String getLargeObjectBucketName() {
        return largeObjectBucketName;
    }

    @Inject
    public void setLargeObjectBucketName(@Named(LARGE_OBJECT_BUCKET) String largeObjectBucketName) {
        this.largeObjectBucketName = largeObjectBucketName;
    }

    public Provider<MongoDatabase> getMongoDatabaseProvider() {
        return mongoDatabaseProvider;
    }

    @Inject
    public void setMongoDatabaseProvider(Provider<MongoDatabase> mongoDatabaseProvider) {
        this.mongoDatabaseProvider = mongoDatabaseProvider;
    }

}
