package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.rest.RestAPIMain.*;
import static com.namazustudios.socialengine.rt.Constants.INSTANCE_DISCOVERY_SERVICE;
import static com.namazustudios.socialengine.rt.remote.RemoteInvoker.REMOTE_INVOKER_MAX_CONNECTIONS;
import static com.namazustudios.socialengine.rt.remote.RemoteInvoker.REMOTE_INVOKER_MIN_CONNECTIONS;
import static com.namazustudios.socialengine.rt.remote.StaticInstanceDiscoveryService.HOST_INFO;
import static com.namazustudios.socialengine.rt.remote.guice.InstanceDiscoveryServiceModule.DiscoveryType.STATIC;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CLUSTER_BIND_ADDRESS;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQInstanceConnectionService.JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS;

public class RestJettyModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(HTTP_BIND_ADDRESS, DEFAULT_BIND_ADDRESS);
        properties.put(HTTP_PORT, Integer.toString(DEFAULT_PORT));
        properties.put(API_CONTEXT, DEFAULT_API_CONTEXT);
        properties.put(REMOTE_INVOKER_MAX_CONNECTIONS, "100");
        properties.put(REMOTE_INVOKER_MIN_CONNECTIONS, "10");
        properties.setProperty(HOST_INFO, "tcp://localhost:28883");
        properties.setProperty(JEROMQ_CLUSTER_BIND_ADDRESS, "");
        properties.setProperty(INSTANCE_DISCOVERY_SERVICE, STATIC.toString());
        properties.setProperty(JEROMQ_CONNECTION_SERVICE_REFRESH_INTERVAL_SECONDS, "10");
        return properties;
    }

}
