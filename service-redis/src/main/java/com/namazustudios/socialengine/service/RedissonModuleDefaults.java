package com.namazustudios.socialengine.service;
import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

/**
 * Created by patricktwohig on 7/28/17.
 */
public class RedissonModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(RedissonClientProvider.REDIS_URL, "redis://127.0.0.1:6379");
        return properties;
    }

}
