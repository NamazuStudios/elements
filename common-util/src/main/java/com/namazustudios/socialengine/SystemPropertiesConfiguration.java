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
 * Created by patricktwohig on 5/9/17.
 */
public class SystemPropertiesConfiguration implements Supplier<Properties> {

    private static final Logger logger = LoggerFactory.getLogger(SystemPropertiesConfiguration.class);

    private final Properties properties;

    public SystemPropertiesConfiguration() {

        final Properties defaultProperties = new Properties(System.getProperties());

        defaultProperties.setProperty(Constants.SHORT_LINK_BASE, "http://localhost:8888/l");
        defaultProperties.setProperty(Constants.QUERY_MAX_RESULTS, Integer.valueOf(20).toString());
        defaultProperties.setProperty(Constants.PASSWORD_DIGEST_ALGORITHM, "SHA-256");
        defaultProperties.setProperty(Constants.PASSWORD_ENCODING, "UTF-8");
        defaultProperties.setProperty(Constants.API_PREFIX, "");
        defaultProperties.setProperty(Constants.API_OUTSIDE_URL, "http://localhost:8080/api");
        defaultProperties.setProperty(Constants.CORS_ALLOWED_ORIGINS, "http://localhost:8888, http://127.0.0.1:8888");

        final Properties properties = new Properties(defaultProperties);
        final File propertiesFile = new File(properties.getProperty(
                Constants.PROPERTIES_FILE,
                Constants.DEFAULT_PROPERTIES_FILE));

        try (final InputStream is = new FileInputStream(propertiesFile)) {
            properties.load(is);
        } catch (IOException ex) {
            logger.warn("Could not load properties.", ex);
        }

        logger.info("Using configuration properties " + properties);
        this.properties = properties;

    }

    public Properties get() {
        return new Properties(this.properties);
    }

}
