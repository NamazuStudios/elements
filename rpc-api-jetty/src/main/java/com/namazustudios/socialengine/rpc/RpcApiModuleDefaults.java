package com.namazustudios.socialengine.rpc;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.*;
import static com.namazustudios.socialengine.rt.Constants.*;
import static com.namazustudios.socialengine.rt.Constants.SRV_SERVERS;
import static com.namazustudios.socialengine.rt.jeromq.JeroMQAsyncConnectionService.ASYNC_CONNECTION_IO_THREADS;
import static com.namazustudios.socialengine.rt.jeromq.ZContextProvider.IO_THREADS;
import static com.namazustudios.socialengine.rt.jeromq.ZContextProvider.MAX_SOCKETS;
import static com.namazustudios.socialengine.rt.remote.JndiSrvInstanceDiscoveryService.SRV_AUTHORITATIVE;
import static com.namazustudios.socialengine.rt.remote.RemoteInvoker.REMOTE_INVOKER_MAX_CONNECTIONS;
import static com.namazustudios.socialengine.rt.remote.RemoteInvoker.REMOTE_INVOKER_MIN_CONNECTIONS;
import static com.namazustudios.socialengine.rt.remote.SimpleRemoteInvokerRegistry.*;
import static com.namazustudios.socialengine.rt.remote.SimpleRemoteInvokerRegistry.DEFAULT_TOTAL_REFRESH_TIMEOUT;
import static com.namazustudios.socialengine.rt.remote.StaticInstanceDiscoveryService.STATIC_HOST_INFO;
import static com.namazustudios.socialengine.rt.remote.guice.InstanceDiscoveryServiceModule.DiscoveryType.STATIC;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CLUSTER_BIND_ADDRESS;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS;
import static java.lang.Runtime.getRuntime;

public class RpcApiModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final var properties = new Properties();
        properties.setProperty(HTTP_PORT, "8083");
        properties.setProperty(HTTP_PATH_PREFIX, "app");
        properties.setProperty(HTTP_TIMEOUT_MSEC, "180000");
        properties.setProperty(MAX_SOCKETS, "500000");
        properties.setProperty(IO_THREADS, Integer.toString(getRuntime().availableProcessors() + 1));
        properties.setProperty(ASYNC_CONNECTION_IO_THREADS, Integer.toString(getRuntime().availableProcessors() + 1));
        properties.setProperty(JEROMQ_CLUSTER_BIND_ADDRESS, "");
        properties.setProperty(JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS, "10");
        properties.setProperty(INSTANCE_DISCOVERY_SERVICE, STATIC.toString());
        properties.setProperty(STATIC_HOST_INFO, "tcp://localhost:28883");
        properties.setProperty(REMOTE_INVOKER_MIN_CONNECTIONS, "10");
        properties.setProperty(REMOTE_INVOKER_MAX_CONNECTIONS, "100");
        properties.setProperty(SRV_QUERY, "_elements._tcp.internal");
        properties.setProperty(SRV_SERVERS, "");
        properties.setProperty(SRV_AUTHORITATIVE, "false");
        properties.setProperty(REFRESH_RATE_SECONDS, String.valueOf(DEFAULT_REFRESH_RATE));
        properties.setProperty(REFRESH_TIMEOUT_SECONDS, String.valueOf(DEFAULT_REFRESH_TIMEOUT));
        properties.setProperty(TOTAL_REFRESH_TIMEOUT_SECONDS, String.valueOf(DEFAULT_TOTAL_REFRESH_TIMEOUT));
        properties.setProperty(CORS_ALLOWED_ORIGINS, "*");
        return properties;
    }

}

