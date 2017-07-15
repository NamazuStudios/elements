package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.Version;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This is an empty interface which is used as a place to house Swagger definitions.
 *
 * Created by patricktwohig on 7/14/17.
 */
@Path("version")
public final class VersionResource {

    public static final String VERSION;

    public static final String REVISION;

    public static final String TIMESTAMP;

    private static final Logger logger = LoggerFactory.getLogger(VersionResource.class);

    private static final String UNKNOWN_PROPERTY = "UNKNOWN";

    static {

        final Properties properties = new Properties();

        try (final InputStream is = VersionResource.class
                .getClassLoader()
                .getResourceAsStream("build.properties")) {

            if (is == null) {
                logger.info("No build.properties found on the classpath.");
            } else {
                properties.load(is);
            }

        } catch (IOException ex) {
            logger.info("Unable to determine server version from build.properties", ex);
        }

        VERSION = properties.getProperty("version", UNKNOWN_PROPERTY);
        REVISION = properties.getProperty("revision", UNKNOWN_PROPERTY);
        TIMESTAMP =  properties.getProperty("timestamp", UNKNOWN_PROPERTY);

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Show Server Version Information",
            notes = "Returns information about the current server version.  This should alwasy return the" +
                    "version metadata.  This information is only known in packaged releases.")
    public static Version getVersion() {
        final Version version = new Version();
        version.setVersion(VERSION);
        version.setRevision(REVISION);
        version.setTimestamp(TIMESTAMP);
        return version;
    }

}
