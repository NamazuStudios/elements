package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer;
import com.namazustudios.socialengine.remote.jeromq.JeroMQNode;
import com.namazustudios.socialengine.rt.Constants;
import com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool;

import java.util.Properties;

import static java.lang.Runtime.*;

public class ApplicationNodeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.setProperty(DynamicConnectionPool.TIMEOUT, "60");
        properties.setProperty(DynamicConnectionPool.MIN_CONNECTIONS, "10");
        properties.setProperty(DynamicConnectionPool.MAX_CONNECTIONS, "10000");
        properties.setProperty(JeroMQConnectionDemultiplexer.BIND_ADDR, "tcp://*:28883");
        properties.setProperty(Constants.SCHEDULER_THREADS, "1");
        properties.setProperty(Constants.HANDLER_TIMEOUT_MSEC, "180000");
        return properties;
    }

}
