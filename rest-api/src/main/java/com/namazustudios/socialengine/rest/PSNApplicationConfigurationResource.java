package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.application.PSNApplicationConfiguration;
import com.namazustudios.socialengine.service.PSNApplicationConfigurationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Handles the management of {@link PSNApplicationConfiguration} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
@Api(value = "PSN Application Configurations",
    description = "Operations for the management of ApplictionProfiles for the Playstation Network.")
@Path("application/{applicationNameOrId}/profile/psn")
public class PSNApplicationConfigurationResource {

    private PSNApplicationConfigurationService psnApplicationConfigurationService;

    /**
     * Gets the specific {@link PSNApplicationConfiguration} instances assocated with the
     * application.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationProfileNameOrId the application profile name or ID
     *
     * @return the {@link PSNApplicationConfiguration} instance
     */
    @GET
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a PSN Application Profile",
                  notes = "Gets a single PSN application based on unique name or ID.")
    public PSNApplicationConfiguration getPSNApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId) {
        return getPsnApplicationConfigurationService().getApplicationConfiguration(applicationNameOrId, applicationProfileNameOrId);
    }

    /**
     * Creates a new {@link PSNApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param psnApplicationProfile the PSN appliation profile object to creates
     *
     * @return the {@link PSNApplicationConfiguration} the PSN Application Profile
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new PSN ApplicationConfiguration",
                  notes = "Creates a new PSN ApplicationConfiguration with the specific ID or application.")
    public PSNApplicationConfiguration createPSNApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final PSNApplicationConfiguration psnApplicationProfile) {
        return getPsnApplicationConfigurationService().createApplicationConfiguration(applicationNameOrId, psnApplicationProfile);
    }

    /**
     * Updates an existing {@link PSNApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param applicationProfileNameOrId the name or identifier of the {@link PSNApplicationConfiguration}
     * @param psnApplicationProfile the PSN appliation profile object to update
     *
     * @return the {@link PSNApplicationConfiguration} the PSN Application Profile
     */
    @PUT
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a PSN ApplicationConfiguration",
                  notes = "Updates an existing PSN Application profile if it is known to the server.")
    public PSNApplicationConfiguration updateApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId,
            final PSNApplicationConfiguration psnApplicationProfile) {
        return getPsnApplicationConfigurationService().updateApplicationConfiguration(
                applicationNameOrId,
                applicationProfileNameOrId,
                psnApplicationProfile);
    }

    /**
     * Deletes an instance of {@link PSNApplicationConfiguration}.
     *
     * @param applicationNameOrId the application ID, or name
     * @param applicationProfileNameOrId the application profile ID, or name
     */
    @DELETE
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a PSN ApplicationConfiguration",
                  notes = "Deletes an existing PSN Application profile if it is known to the server.")
    public void deletePSNApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId) {
        getPsnApplicationConfigurationService().deleteApplicationConfiguration(applicationNameOrId, applicationProfileNameOrId);
    }

    public PSNApplicationConfigurationService getPsnApplicationConfigurationService() {
        return psnApplicationConfigurationService;
    }

    @Inject
    public void setPsnApplicationConfigurationService(PSNApplicationConfigurationService psnApplicationConfigurationService) {
        this.psnApplicationConfigurationService = psnApplicationConfigurationService;
    }

}
