package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.rt.Constants;
import com.namazustudios.socialengine.rt.jeromq.ZContextProvider;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.*;
import static com.namazustudios.socialengine.rt.jeromq.ConnectionPool.*;
import static com.namazustudios.socialengine.rt.jeromq.ZContextProvider.IO_THREADS;
import static com.namazustudios.socialengine.rt.jeromq.ZContextProvider.MAX_SOCKETS;
import static java.lang.Runtime.getRuntime;

public class AppServeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(HTTP_TUNNEL_PORT, "8080");
        properties.setProperty(TIMEOUT, "60");
        properties.setProperty(MIN_CONNECTIONS, "10");
        properties.setProperty(MAX_CONNECTIONS, "10000");
        properties.setProperty(Constants.HTTP_TIMEOUT_MSEC, "180000");
        properties.setProperty(MAX_SOCKETS, "500000");
        properties.setProperty(IO_THREADS, Integer.toString(getRuntime().availableProcessors() + 1));
        return properties;
    }

}
