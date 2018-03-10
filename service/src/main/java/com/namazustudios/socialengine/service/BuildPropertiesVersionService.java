package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Version;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildPropertiesVersionService implements VersionService {

    public static final String VERSION;

    public static final String REVISION;

    public static final String TIMESTAMP;

    private static final Logger logger = LoggerFactory.getLogger(BuildPropertiesVersionService.class);

    private static final String UNKNOWN_PROPERTY = "UNKNOWN";

    static {

        final Properties properties = new Properties();

        try (final InputStream is = BuildPropertiesVersionService.class
                .getClassLoader()
                .getResourceAsStream("build.properties")) {

            if (is == null) {
                logger.info("No build.properties found on the classpath.");
            } else {
                properties.load(is);
            }

        } catch (IOException ex) {
            logger.warn("Unable to determine server version from build.properties", ex);
        }

        VERSION = properties.getProperty("version", UNKNOWN_PROPERTY);
        REVISION = properties.getProperty("revision", UNKNOWN_PROPERTY);
        TIMESTAMP =  properties.getProperty("timestamp", UNKNOWN_PROPERTY);

        logger.info("Version {}.  Revision {}.  Timestamp {}.", VERSION, REVISION, TIMESTAMP);

    }

    public Version getVersion() {
        final Version version = new Version();
        version.setVersion(VERSION);
        version.setRevision(REVISION);
        version.setTimestamp(TIMESTAMP);
        return version;
    }

}
