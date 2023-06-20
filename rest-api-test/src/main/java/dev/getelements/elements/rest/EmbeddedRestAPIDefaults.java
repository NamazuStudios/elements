package dev.getelements.elements.rest;

import dev.getelements.elements.config.ModuleDefaults;

import java.util.Properties;

import static dev.getelements.elements.Constants.HTTP_PATH_PREFIX;

public class EmbeddedRestAPIDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final var properties = new Properties();
        properties.put(HTTP_PATH_PREFIX, "/");
        return properties;
    }

}
