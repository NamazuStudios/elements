package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.PrivateModule;
import com.mongodb.client.gridfs.GridFSBucket;
import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.dao.mongo.largeobject.GridFSLargeObjectBucket;
import dev.getelements.elements.dao.mongo.provider.LargeObjectGridFSBucketProvider;

/**
 * Created by patricktwohig on 6/29/17.
 */
public class MongoGridFSLargeObjectBucketModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(LargeObjectBucket.class).to(GridFSLargeObjectBucket.class);
        bind(GridFSBucket.class).toProvider(LargeObjectGridFSBucketProvider.class).asEagerSingleton();
        expose(LargeObjectBucket.class);
    }

}
