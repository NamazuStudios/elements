package com.namazustudios.socialengine.jnlua;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildVersionInfo {

    private static final Logger logger = LoggerFactory.getLogger(BuildVersionInfo.class);

    public static final String VERSION;

    public static final String REVISION;

    public static final String TIMESTAMP;

    private static final String UNKNOWN_PROPERTY = "UNKNOWN";

    static {

        final Properties properties = new Properties();

        try (final InputStream is = BuildVersionInfo.class
                .getClassLoader()
                .getResourceAsStream("build.properties")) {

            if (is == null) {
                logger.warn("No build.properties found on the classpath.");
            } else {
                properties.load(is);
            }

        } catch (IOException ex) {
            logger.warn("Unable to determine server version from build.properties", ex);
        }

        VERSION = properties.getProperty("version", UNKNOWN_PROPERTY);
        REVISION = properties.getProperty("revision", UNKNOWN_PROPERTY);
        TIMESTAMP =  properties.getProperty("timestamp", UNKNOWN_PROPERTY);

    }

}
