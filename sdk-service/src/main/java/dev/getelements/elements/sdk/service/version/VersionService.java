package dev.getelements.elements.sdk.service.version;

import dev.getelements.elements.sdk.model.Version;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Gets the {@link Version} of the current build.
 *
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface VersionService {

    /**
     * Returns the current {@link Version} of the current built.
     *
     * @return the {@link Version}
     */
    Version getVersion();

}
