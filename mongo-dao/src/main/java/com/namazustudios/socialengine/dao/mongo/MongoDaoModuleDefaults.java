package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.ModuleDefaults;
import com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider;
import com.namazustudios.socialengine.dao.mongo.provider.MongoDatabaseProvider;

import java.util.Properties;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoDaoModuleDefaults implements ModuleDefaults {

    public static final int DEFAULT_FALLOFF_TIME_MS = 100;

    public static final int OPTISMITIC_RETRY_COUNT = 10;

    @Override
    public Properties get() {
        final Properties defaultProperties = new Properties(System.getProperties());
        defaultProperties.setProperty(MongoClientProvider.MONGO_DB_URLS, "mongo://localhost");
        defaultProperties.setProperty(MongoDatabaseProvider.DATABASE_NAME, "socialengine");
        defaultProperties.setProperty(MongoConcurrentUtils.FALLOFF_TIME_MS, Integer.toString(DEFAULT_FALLOFF_TIME_MS));
        defaultProperties.setProperty(MongoConcurrentUtils.OPTIMISTIC_RETRY_COUNT, Integer.toString(OPTISMITIC_RETRY_COUNT));
        return defaultProperties;
    }

}
