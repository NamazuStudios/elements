package com.namazustudios.socialengine;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Implements the default configuration scheme.  In addition to providing a set of {@link Properties}
 * that has a set of "out of the box" defaults, this will attempt to load properties from either
 * system properties or a properties file defined by the system properties.
 *
 * Created by patricktwohig on 5/9/17.
 */
public class DefaultConfigurationSupplier implements Supplier<Properties> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultConfigurationSupplier.class);

    private final Properties properties;

    public DefaultConfigurationSupplier() {

        final Properties defaultProperties = scanForDefaults();
        final Properties properties = new Properties(defaultProperties);

        final File propertiesFile = new File(System.getProperties().getProperty(
                Constants.PROPERTIES_FILE,
                Constants.DEFAULT_PROPERTIES_FILE));

        try (final InputStream is = new FileInputStream(propertiesFile)) {
            properties.load(is);
            logger.info("Loaded properties from file: {}", propertiesFile.getAbsolutePath());
        } catch (IOException ex) {
            properties.putAll(System.getProperties());
            logger.info("Could not load properties.  Using system properties.", ex);
        }

        logger.info("Using configuration properties " + properties);
        this.properties = properties;

    }

    public Properties get() {
        return new Properties(this.properties);
    }

    private Properties scanForDefaults() {

        final Reflections reflections = new Reflections("com.namazustudios");
        final Set<Class<? extends ModuleDefaults>> classSet = reflections.getSubTypesOf(ModuleDefaults.class);

        final Properties defaultProperties = new Properties();

        for (final Class<? extends ModuleDefaults> cls : classSet) {
            try {

                logger.info("Loading default properties for {}", cls);

                final ModuleDefaults defaults = cls.newInstance();
                defaultProperties.putAll(defaults.get());

            } catch (InstantiationException e) {
                logger.error("Could not build module defaults.", e);
            } catch (IllegalAccessException e) {
                logger.error("Could not build module defaults.", e);
            }
        }

        return defaultProperties;

    }

}
