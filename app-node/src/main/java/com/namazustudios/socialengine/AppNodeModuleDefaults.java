package com.namazustudios.socialengine;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.remote.jeromq.JeroMQNode;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool;

import java.util.Properties;

public class AppNodeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.setProperty(JeroMQNode.BIND_ADDRESS, "tcp://*:28883");
        properties.setProperty(JeroMQNode.NUMBER_OF_DISPATCHERS, "500");
        properties.setProperty(DynamicConnectionPool.MIN_CONNECTIONS, "500");
        return properties;
    }

}
