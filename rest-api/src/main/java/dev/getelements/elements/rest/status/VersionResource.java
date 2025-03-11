package dev.getelements.elements.rest.status;

import dev.getelements.elements.sdk.model.Version;
import dev.getelements.elements.sdk.service.version.VersionService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * This is an empty interface which is used as a place to house Swagger definitions.
 *
 * Created by patricktwohig on 7/14/17.
 */
@Path("version")
public final class VersionResource {

    private VersionService versionService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Show Server Version Information",
            description = "Returns information about the current server version.  This should always return the" +
                    "version metadata.  This information is only known in packaged releases.")
    public Version getVersion() {
        return getVersionService().getVersion();
    }

    public VersionService getVersionService() {
        return versionService;
    }

    @Inject
    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

}
