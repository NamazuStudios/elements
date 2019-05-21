package com.namazustudios.socialengine.dao.rt;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.GIT_STORAGE_DIRECTORY;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionService.APPLICATION_NODE_FQDN;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.MAX_CONNECTIONS;
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
        properties.put(APPLICATION_NODE_FQDN, "appnode.tcp.namazustudios.com.");
        properties.setProperty(TIMEOUT, "60");
        properties.setProperty(MIN_CONNECTIONS, "10");
        properties.setProperty(MAX_CONNECTIONS, "10000");
        return properties;
    }

}
