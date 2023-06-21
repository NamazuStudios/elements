package dev.getelements.elements.jetty;

import dev.getelements.elements.config.ModuleDefaults;

import java.util.Properties;

import static dev.getelements.elements.Constants.HTTP_PATH_PREFIX;
import static dev.getelements.elements.Constants.HTTP_PORT;

public class ElementsModuleDefaults implements ModuleDefaults {
    @Override
    public Properties get() {
        final var properties = new Properties();
        properties.put(HTTP_PORT, "8081");
        properties.put(HTTP_PATH_PREFIX, "/");
        return properties;
    }

}
