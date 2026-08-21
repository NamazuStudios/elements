package dev.getelements.elements.service;

import dev.getelements.elements.config.ModuleDefaults;

import java.util.Properties;

import static dev.getelements.elements.rt.Constants.*;
import static dev.getelements.elements.rt.remote.JndiSrvInstanceDiscoveryService.SRV_AUTHORITATIVE;
import static dev.getelements.elements.rt.remote.RemoteInvoker.REMOTE_INVOKER_MAX_CONNECTIONS;
import static dev.getelements.elements.rt.remote.RemoteInvoker.REMOTE_INVOKER_MIN_CONNECTIONS;
import static dev.getelements.elements.rt.remote.SimpleRemoteInvokerRegistry.*;
import static dev.getelements.elements.rt.remote.SimpleRemoteInvokerRegistry.DEFAULT_TOTAL_REFRESH_TIMEOUT;
import static dev.getelements.elements.rt.remote.StaticInstanceDiscoveryService.STATIC_HOST_INFO;
import static dev.getelements.elements.rt.remote.guice.InstanceDiscoveryServiceModule.DiscoveryType.STATIC;

public class ServiceTestModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(REMOTE_INVOKER_MAX_CONNECTIONS, "100");
        properties.put(REMOTE_INVOKER_MIN_CONNECTIONS, "10");
        properties.put(STATIC_HOST_INFO, "ws://localhost:28883");
        properties.put(INSTANCE_DISCOVERY_SERVICE, STATIC.toString());
        properties.put(SRV_QUERY, "_elements._tcp.internal");
        properties.put(SRV_SERVERS, "");
        properties.setProperty(SRV_AUTHORITATIVE, "false");
        properties.setProperty(REFRESH_RATE_SECONDS, String.valueOf(DEFAULT_REFRESH_RATE));
        properties.setProperty(REFRESH_TIMEOUT_SECONDS, String.valueOf(DEFAULT_REFRESH_TIMEOUT));
        properties.setProperty(TOTAL_REFRESH_TIMEOUT_SECONDS, String.valueOf(DEFAULT_TOTAL_REFRESH_TIMEOUT));
        return properties;
    }

}
