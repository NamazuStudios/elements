package dev.getelements.elements.rest;

import dev.getelements.elements.config.ModuleDefaults;

import java.util.Properties;

import static dev.getelements.elements.Constants.*;
import static dev.getelements.elements.rest.RestAPIMain.*;
import static dev.getelements.elements.rt.Constants.*;
import static dev.getelements.elements.rt.jeromq.JeroMQAsyncConnectionService.ASYNC_CONNECTION_IO_THREADS;
import static dev.getelements.elements.rt.jeromq.ZContextProvider.IO_THREADS;
import static dev.getelements.elements.rt.jeromq.ZContextProvider.MAX_SOCKETS;
import static dev.getelements.elements.rt.remote.JndiSrvInstanceDiscoveryService.SRV_AUTHORITATIVE;
import static dev.getelements.elements.rt.remote.RemoteInvoker.REMOTE_INVOKER_MAX_CONNECTIONS;
import static dev.getelements.elements.rt.remote.RemoteInvoker.REMOTE_INVOKER_MIN_CONNECTIONS;
import static dev.getelements.elements.rt.remote.SimpleRemoteInvokerRegistry.*;
import static dev.getelements.elements.rt.remote.SimpleRemoteInvokerRegistry.DEFAULT_TOTAL_REFRESH_TIMEOUT;
import static dev.getelements.elements.rt.remote.StaticInstanceDiscoveryService.STATIC_HOST_INFO;
import static dev.getelements.elements.rt.remote.guice.InstanceDiscoveryServiceModule.DiscoveryType.STATIC;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CLUSTER_BIND_ADDRESS;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS;
import static java.lang.Runtime.getRuntime;

public class RestJettyModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(HTTP_PORT, Integer.toString(DEFAULT_PORT));
        properties.put(HTTP_PATH_PREFIX, DEFAULT_API_CONTEXT);
        properties.put(REMOTE_INVOKER_MAX_CONNECTIONS, "100");
        properties.put(REMOTE_INVOKER_MIN_CONNECTIONS, "10");
        properties.put(STATIC_HOST_INFO, "tcp://localhost:28883");
        properties.put(JEROMQ_CLUSTER_BIND_ADDRESS, "");
        properties.put(INSTANCE_DISCOVERY_SERVICE, STATIC.toString());
        properties.put(JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS, "10");
        properties.put(IO_THREADS, Integer.toString(getRuntime().availableProcessors() + 1));
        properties.setProperty(ASYNC_CONNECTION_IO_THREADS, Integer.toString(getRuntime().availableProcessors() + 1));
        properties.put(MAX_SOCKETS, "500000");
        properties.put(SRV_QUERY, "_elements._tcp.internal");
        properties.put(SRV_SERVERS, "");
        properties.setProperty(SRV_AUTHORITATIVE, "false");
        properties.setProperty(REFRESH_RATE_SECONDS, String.valueOf(DEFAULT_REFRESH_RATE));
        properties.setProperty(REFRESH_TIMEOUT_SECONDS, String.valueOf(DEFAULT_REFRESH_TIMEOUT));
        properties.setProperty(TOTAL_REFRESH_TIMEOUT_SECONDS, String.valueOf(DEFAULT_TOTAL_REFRESH_TIMEOUT));
        return properties;
    }

}
