package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.config.ModuleDefaults;
import dev.getelements.elements.dao.mongo.provider.LargeObjectGridFSBucketProvider;

import java.util.Properties;

/**
 * Created by patricktwohig on 6/29/17.
 */
public class MongoLargeObjectModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.setProperty(LargeObjectGridFSBucketProvider.LARGE_OBJECT_BUCKET, "large_object");
        return properties;
   }

}
