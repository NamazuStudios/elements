package dev.getelements.elements.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Represents the Elements system version. When this class loads, it will populate the information from the build
 * properties. If unavailable, this will set the version to UNKNOWN.
 *
 * @param version the version code corresponding to the release
 * @param revision the revision or specific commit from which this was made
 * @param timestamp the timestamp of the build
 */
public record SystemVersion(String version, String revision, String timestamp) {

    private static final Logger logger = LoggerFactory.getLogger(SystemVersion.class);

    private static final String UNKNOWN_PROPERTY = "UNKNOWN";

    /**
     * Constant for the UNKNOWN version.
     */
    public static final SystemVersion UNKNOWN = new SystemVersion(UNKNOWN_PROPERTY, UNKNOWN_PROPERTY, UNKNOWN_PROPERTY);

    /**
     * Constant for the CURRENT version. May be equal to {@link #UNKNOWN} if the version is not available.
     */
    public static final SystemVersion CURRENT = loadCurrent();

    private static SystemVersion loadCurrent() {

        final Properties properties = new Properties();

        try (final InputStream is = SystemVersion.class
                .getClassLoader()
                .getResourceAsStream("build.properties")) {

            if (is == null) {
                logger.info("No build.properties found on the classpath.");
                return UNKNOWN;
            } else {
                properties.load(is);
            }

        } catch (IOException ex) {
            logger.warn("Unable to determine server version from build.properties", ex);
            return UNKNOWN;
        }

        final var version = properties.getProperty("version", UNKNOWN_PROPERTY);
        final var revision = properties.getProperty("revision", UNKNOWN_PROPERTY);
        final var timestamp = properties.getProperty("timestamp", UNKNOWN_PROPERTY);

        return new SystemVersion(version, revision, timestamp);

    }

    /**
     * Logs this specific version to the logger as an INFO level message.
     */
    public void logVersion() {
        logger.info("Namazu Elements (tm) Version {}.  Revision {}.  Timestamp {}.",
                version(),
                revision(),
                timestamp()
        );
    }

}
