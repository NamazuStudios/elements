package com.namazustudios.socialengine.rest.application;

import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;
import com.namazustudios.socialengine.service.IosApplicationConfigurationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Handles the management of {@link IosApplicationConfiguration} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
@Api(
    value = "iOS Application Configuration",
    description = "Operations for the management of ApplictionProfiles for iOS Applications.")
@Path("application/{applicationNameOrId}/configuration/ios")
public class IoSApplicationConfigurationResource {

    private IosApplicationConfigurationService iosApplicationConfigurationService;

    /**
     * Gets the specific {@link IosApplicationConfiguration} instances assocated with the
     * application.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationProfileNameOrId the application profile name or ID
     *
     * @return the {@link IosApplicationConfiguration} instance
     */
    @GET
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Gets a iOS Application Profile",
        notes = "Gets a single iOS application based on unique name or ID.")
    public IosApplicationConfiguration getIosApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId) {
        return getIosApplicationConfigurationService().getApplicationConfiguration(applicationNameOrId, applicationProfileNameOrId);
    }

    /**
     * Creates a new {@link IosApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param iosApplicationProfiles the iOS appliation profile object to creates
     *
     * @return the {@link IosApplicationConfiguration} the iOS Application Profile
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Creates a new iOS ApplicationConfiguration",
        notes = "Creates a new iOS ApplicationConfiguration with the specific ID or application.")
    public IosApplicationConfiguration createIosApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final IosApplicationConfiguration iosApplicationProfiles) {
        return getIosApplicationConfigurationService().createApplicationConfiguration(applicationNameOrId, iosApplicationProfiles);
    }

    /**
     * Updates an existing {@link IosApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param applicationProfileNameOrId the name or identifier of the {@link IosApplicationConfiguration}
     * @param iosApplicationProfile the iOS application profile object to update
     *
     * @return the {@link IosApplicationConfiguration} the iOS Application Profile
     */
    @PUT
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Updates a iOS ApplicationConfiguration",
            notes = "Updates an existing iOS Application profile if it is known to the server.")
    public IosApplicationConfiguration updateApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId,
            final IosApplicationConfiguration iosApplicationProfile) {
        return getIosApplicationConfigurationService().updateApplicationConfiguration(
                applicationNameOrId,
                applicationProfileNameOrId,
                iosApplicationProfile);
    }

    /**
     * Deletes an instance of {@link IosApplicationConfiguration}.
     *
     * @param applicationNameOrId the application ID, or name
     * @param applicationProfileNameOrId the application profile ID, or name
     */
    @DELETE
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Deletes a iOS ApplicationConfiguration",
            notes = "Deletes an existing iOS Application profile if it is known to the server.")
    public void deleteIosApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId) {
        getIosApplicationConfigurationService().deleteApplicationConfiguration(applicationNameOrId, applicationProfileNameOrId);
    }

    public IosApplicationConfigurationService getIosApplicationConfigurationService() {
        return iosApplicationConfigurationService;
    }

    @Inject
    public void setIosApplicationConfigurationService(IosApplicationConfigurationService iosApplicationConfigurationService) {
        this.iosApplicationConfigurationService = iosApplicationConfigurationService;
    }

}
