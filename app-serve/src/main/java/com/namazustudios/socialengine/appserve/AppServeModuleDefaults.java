package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.*;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.CONNECT_ADDR;

public class AppServeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(HTTP_TUNNEL_PORT, "8080");
        properties.put(CONNECT_ADDR, "tcp://localhost:28883");
        return properties;
    }

}
