package dev.getelements.elements.service.version;

import dev.getelements.elements.sdk.model.Version;
import dev.getelements.elements.sdk.service.version.VersionService;

import static dev.getelements.elements.sdk.SystemVersion.CURRENT;

public class BuildPropertiesVersionService implements VersionService {

     public Version getVersion() {
        final Version version = new Version();
        version.setVersion(CURRENT.version());
        version.setRevision(CURRENT.revision());
        version.setTimestamp(CURRENT.timestamp());
        return version;
    }

}
