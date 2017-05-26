package com.namazustudios.socialengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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

        final Properties defaultProperties = new Properties();

        defaultProperties.setProperty(Constants.SHORT_LINK_BASE, "http://localhost:8888/l");
        defaultProperties.setProperty(Constants.QUERY_MAX_RESULTS, Integer.valueOf(20).toString());
        defaultProperties.setProperty(Constants.PASSWORD_DIGEST_ALGORITHM, "SHA-256");
        defaultProperties.setProperty(Constants.PASSWORD_ENCODING, "UTF-8");
        defaultProperties.setProperty(Constants.API_PREFIX, "rest");
        defaultProperties.setProperty(Constants.API_OUTSIDE_URL, "http://localhost:8080/api/rest");
        defaultProperties.setProperty(Constants.CORS_ALLOWED_ORIGINS, "http://localhost:8888, http://127.0.0.1:8888");

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

}
