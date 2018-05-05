package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.FALLOFF_TIME_MAX_MS;
import static com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.FALLOFF_TIME_MIN_MS;
import static com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.OPTIMISTIC_RETRY_COUNT;
import static com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider.MONGO_DB_URLS;
import static com.namazustudios.socialengine.dao.mongo.provider.MongoDatabaseProvider.DATABASE_NAME;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoDaoModuleDefaults implements ModuleDefaults {

    public static final int DEFAULT_FALLOFF_TIME_MIN_MS = 50;

    public static final int DEFAULT_FALLOFF_TIME_MAX_MS = 150;

    public static final int OPTISMITIC_RETRY_COUNT = 10;

    @Override
    public Properties get() {
        final Properties defaultProperties = new Properties(System.getProperties());
        defaultProperties.setProperty(MONGO_DB_URLS, "mongo://localhost");
        defaultProperties.setProperty(DATABASE_NAME, "socialengine");
        defaultProperties.setProperty(FALLOFF_TIME_MIN_MS, Integer.toString(DEFAULT_FALLOFF_TIME_MIN_MS));
        defaultProperties.setProperty(FALLOFF_TIME_MAX_MS, Integer.toString(DEFAULT_FALLOFF_TIME_MAX_MS));
        defaultProperties.setProperty(OPTIMISTIC_RETRY_COUNT, Integer.toString(OPTISMITIC_RETRY_COUNT));
        return defaultProperties;
    }

}
