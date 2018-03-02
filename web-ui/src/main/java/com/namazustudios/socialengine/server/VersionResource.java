package com.namazustudios.socialengine.server;

import com.namazustudios.socialengine.model.Version;
import com.namazustudios.socialengine.service.VersionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("version")
@Api("Sever Version Metadata")
public class VersionResource {

    private VersionService versionService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Show Server Version Information",
            notes = "Returns information about the current server version.  This should alwasy return the" +
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
