package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.dao.mongo.provider.MongoDirectoryProvider;

import java.util.Properties;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoSearchModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties defaultProperties = new Properties(System.getProperties());
        defaultProperties.setProperty(MongoDirectoryProvider.LOCK_COLLECTION, "fts.locks");
        defaultProperties.setProperty(MongoDirectoryProvider.SEARCH_INDEX_BUCKET, "fts.index");
        return defaultProperties;
    }

}
