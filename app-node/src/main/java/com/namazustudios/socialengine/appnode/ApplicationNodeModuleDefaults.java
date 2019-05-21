package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.rt.HandlerContext;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;

import java.util.Properties;

import static com.namazustudios.socialengine.appnode.Constants.*;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQDemultiplexedConnectionService.*;
import static com.namazustudios.socialengine.rt.Constants.*;
import static com.namazustudios.socialengine.rt.HandlerContext.*;
import static com.namazustudios.socialengine.rt.jeromq.ConnectionPool.*;

public class ApplicationNodeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.setProperty(TIMEOUT, "60");
        properties.setProperty(MIN_CONNECTIONS, "10");
        properties.setProperty(MAX_CONNECTIONS, "10000");
        properties.setProperty(BIND_PORT, "28883");
        properties.setProperty(CONTROL_BIND_PORT, "20883");
        properties.setProperty(APPLICATION_NODE_FQDN, "appnode.tcp.namazustudios.com.");
        properties.setProperty(CONTROL_REQUEST_TIMEOUT, "1000");
        properties.setProperty(SCHEDULER_THREADS, Integer.toString(Runtime.getRuntime().availableProcessors()) + 1);
        properties.setProperty(HANDLER_TIMEOUT_MSEC, "180000");
        properties.setProperty(STORAGE_BASE_DIRECTORY, "storage.xodus");
        return properties;
    }

}
