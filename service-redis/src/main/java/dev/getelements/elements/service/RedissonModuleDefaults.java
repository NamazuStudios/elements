package dev.getelements.elements.service;

import dev.getelements.elements.config.ModuleDefaults;

import java.util.Properties;

import static dev.getelements.elements.service.RedissonClientProvider.REDIS_URL;
import static dev.getelements.elements.service.SpotifySrvRedissonClientProvider.SRV_SERVERS;

/**
 * Created by patricktwohig on 7/28/17.
 */
public class RedissonModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(REDIS_URL, "redis://127.0.0.1:6379");
        properties.put(SRV_SERVERS, "");
        return properties;
    }

}
