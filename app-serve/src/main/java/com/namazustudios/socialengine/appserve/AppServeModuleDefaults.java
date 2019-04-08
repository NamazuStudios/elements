package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.rt.Constants;
import com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.*;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionsManager.APPLICATION_NODE_FQDN;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQMultiplexedConnectionsManager.CONNECT_ADDR;
import static com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.*;

public class AppServeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(HTTP_TUNNEL_PORT, "8080");
        properties.put(CONNECT_ADDR, "tcp://localhost:28883");
        properties.put(APPLICATION_NODE_FQDN, "appnode.tcp.namazustudios.com.");
        properties.setProperty(TIMEOUT, "60");
        properties.setProperty(MIN_CONNECTIONS, "10");
        properties.setProperty(DynamicConnectionPool.MAX_CONNECTIONS, "10000");
        properties.setProperty(Constants.HTTP_TIMEOUT_MSEC, "180000");
        return properties;
    }

}
