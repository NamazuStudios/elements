package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer;
import com.namazustudios.socialengine.remote.jeromq.JeroMQNode;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool;

import java.util.Properties;

public class ApplicationNodeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.setProperty(JeroMQNode.NUMBER_OF_DISPATCHERS, "100");
        properties.setProperty(DynamicConnectionPool.TIMEOUT, "60");
        properties.setProperty(DynamicConnectionPool.MIN_CONNECTIONS, "10");
        properties.setProperty(DynamicConnectionPool.MAX_CONNECTIONS, "10000");
        properties.setProperty(JeroMQConnectionDemultiplexer.BIND_ADDR, "tcp://*:28883");
        return properties;
    }

}
