package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.rt.HandlerContext;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;

import java.util.Properties;

import static com.namazustudios.socialengine.appnode.Constants.*;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer.BIND_ADDR;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer.CONTROL_BIND_ADDR;
import static com.namazustudios.socialengine.rt.Constants.*;
import static com.namazustudios.socialengine.rt.EventContext.EVENT_TIMEOUT_MSEC;
import static com.namazustudios.socialengine.rt.HandlerContext.*;

public class ApplicationNodeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.setProperty(ConnectionPool.TIMEOUT, "60");
        properties.setProperty(ConnectionPool.MIN_CONNECTIONS, "1000");
        properties.setProperty(ConnectionPool.MAX_CONNECTIONS, "450000");
        properties.setProperty(BIND_ADDR, "tcp://*:28883");
        properties.setProperty(CONTROL_BIND_ADDR, "tcp://*:20883");
        properties.setProperty(CONTROL_REQUEST_TIMEOUT, "1000");
        properties.setProperty(SCHEDULER_THREADS, Integer.toString(Runtime.getRuntime().availableProcessors()) + 1);
        properties.setProperty(HANDLER_TIMEOUT_MSEC, "180000");
        properties.setProperty(EVENT_TIMEOUT_MSEC, "180000");
        properties.setProperty(STORAGE_BASE_DIRECTORY, "storage.xodus");
        return properties;
    }

}
