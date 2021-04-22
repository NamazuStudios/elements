package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.rt.Constants;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.HTTP_PATH_PREFIX;
import static com.namazustudios.socialengine.Constants.HTTP_PORT;
import static com.namazustudios.socialengine.appserve.DispatcherAppProvider.LEGACY_ENDPOINT;
import static com.namazustudios.socialengine.appserve.DispatcherAppProvider.VERSION_ENDPOINT;
import static com.namazustudios.socialengine.rt.Constants.HTTP_TIMEOUT_MSEC;
import static com.namazustudios.socialengine.rt.Constants.INSTANCE_DISCOVERY_SERVICE;
import static com.namazustudios.socialengine.rt.jeromq.ZContextProvider.IO_THREADS;
import static com.namazustudios.socialengine.rt.jeromq.ZContextProvider.MAX_SOCKETS;
import static com.namazustudios.socialengine.rt.remote.RemoteInvoker.REMOTE_INVOKER_MAX_CONNECTIONS;
import static com.namazustudios.socialengine.rt.remote.RemoteInvoker.REMOTE_INVOKER_MIN_CONNECTIONS;
import static com.namazustudios.socialengine.rt.remote.SpotifySrvInstanceDiscoveryService.SRV_QUERY;
import static com.namazustudios.socialengine.rt.remote.SpotifySrvInstanceDiscoveryService.SRV_SERVERS;
import static com.namazustudios.socialengine.rt.remote.StaticInstanceDiscoveryService.STATIC_HOST_INFO;
import static com.namazustudios.socialengine.rt.remote.guice.InstanceDiscoveryServiceModule.DiscoveryType.STATIC;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CLUSTER_BIND_ADDRESS;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS;
import static java.lang.Runtime.getRuntime;

public class AppServeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final var properties = new Properties();
        properties.setProperty(HTTP_PORT, "8083");
        properties.setProperty(HTTP_PATH_PREFIX, "app-serve");
        properties.setProperty(LEGACY_ENDPOINT, Boolean.TRUE.toString());
        properties.setProperty(VERSION_ENDPOINT, "version");
        properties.setProperty(HTTP_TIMEOUT_MSEC, "180000");
        properties.setProperty(MAX_SOCKETS, "500000");
        properties.setProperty(IO_THREADS, Integer.toString(getRuntime().availableProcessors() + 1));
        properties.setProperty(JEROMQ_CLUSTER_BIND_ADDRESS, "");
        properties.setProperty(JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS, "10");
        properties.setProperty(INSTANCE_DISCOVERY_SERVICE, STATIC.toString());
        properties.setProperty(STATIC_HOST_INFO, "tcp://localhost:28883");
        properties.setProperty(REMOTE_INVOKER_MIN_CONNECTIONS, "10");
        properties.setProperty(REMOTE_INVOKER_MAX_CONNECTIONS, "100");
        properties.setProperty(SRV_QUERY, "_elements._tcp.internal");
        properties.setProperty(SRV_SERVERS, "");
        return properties;
    }

}
