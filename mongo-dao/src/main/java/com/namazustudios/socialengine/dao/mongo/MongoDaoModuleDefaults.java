package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.FALLOFF_TIME_MAX_MS;
import static com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.FALLOFF_TIME_MIN_MS;
import static com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.OPTIMISTIC_RETRY_COUNT;
import static com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider.*;
import static com.namazustudios.socialengine.dao.mongo.provider.MongoDatastoreProvider.DATABASE_NAME;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoDaoModuleDefaults implements ModuleDefaults {

    public static final int OPTISMITIC_RETRY_COUNT = 10;

    public static final int DEFAULT_FALLOFF_TIME_MIN_MS = 50;

    public static final int DEFAULT_FALLOFF_TIME_MAX_MS = 150;

    @Override
    public Properties get() {
        final Properties defaultProperties = new Properties(System.getProperties());
        defaultProperties.setProperty(DATABASE_NAME, "elements");
        defaultProperties.setProperty(MONGO_CLIENT_URI, "mongodb://localhost");
        defaultProperties.setProperty(FALLOFF_TIME_MIN_MS, Integer.toString(DEFAULT_FALLOFF_TIME_MIN_MS));
        defaultProperties.setProperty(FALLOFF_TIME_MAX_MS, Integer.toString(DEFAULT_FALLOFF_TIME_MAX_MS));
        defaultProperties.setProperty(OPTIMISTIC_RETRY_COUNT, Integer.toString(OPTISMITIC_RETRY_COUNT));
        return defaultProperties;
    }

}
