package dev.getelements.elements.jetty;

import dev.getelements.elements.config.ModuleDefaults;

import java.util.Properties;

import static dev.getelements.elements.sdk.model.Constants.HTTP_PATH_PREFIX;
import static dev.getelements.elements.sdk.model.Constants.HTTP_PORT;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQSecurityProvider.JEROMQ_ALLOW_PLAIN_TRAFFIC;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQSecurityProvider.JEROMQ_SERVER_SECURITY_CHAIN_PEM_FILE;

public class ElementsModuleDefaults implements ModuleDefaults {
    @Override
    public Properties get() {
        final var properties = new Properties();
        properties.put(HTTP_PORT, "8080");
        properties.put(HTTP_PATH_PREFIX, "/");
        properties.put(JEROMQ_ALLOW_PLAIN_TRAFFIC, "true");
        properties.put(JEROMQ_SERVER_SECURITY_CHAIN_PEM_FILE, "");
        return properties;
    }

}
