package com.namazustudios.socialengine.dao.rt;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.GIT_STORAGE_DIRECTORY;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.CONNECT_ADDR;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class RTDaoModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(GIT_STORAGE_DIRECTORY, "repositories");
        properties.put(CONNECT_ADDR, "tcp://localhost:28883");
        properties.setProperty(ConnectionPool.TIMEOUT, "60");
        properties.setProperty(ConnectionPool.MIN_CONNECTIONS, "10");
        properties.setProperty(ConnectionPool.MAX_CONNECTIONS, "10000");
        return properties;
    }

}
