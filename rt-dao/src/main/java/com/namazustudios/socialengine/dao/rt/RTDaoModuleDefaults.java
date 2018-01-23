package com.namazustudios.socialengine.dao.rt;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.GIT_STORAGE_DIRECTORY;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.CONNECT_ADDR;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.MIN_CONNECTIONS;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.TIMEOUT;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class RTDaoModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(GIT_STORAGE_DIRECTORY, "repositories");
        properties.put(CONNECT_ADDR, "tcp://localhost:28883");
        properties.setProperty(TIMEOUT, "60");
        properties.setProperty(MIN_CONNECTIONS, "10");
        return properties;
    }

}
