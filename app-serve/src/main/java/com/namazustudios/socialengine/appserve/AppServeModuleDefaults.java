package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.rt.Constants;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.*;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.CONNECT_ADDR;

public class AppServeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(HTTP_TUNNEL_PORT, "8080");
        properties.put(CONNECT_ADDR, "tcp://localhost:28883");
        properties.setProperty(ConnectionPool.TIMEOUT, "60");
        properties.setProperty(ConnectionPool.MIN_CONNECTIONS, "10");
        properties.setProperty(ConnectionPool.MAX_CONNECTIONS, "10000");
        properties.setProperty(Constants.HTTP_TIMEOUT_MSEC, "180000");
        return properties;
    }

}
