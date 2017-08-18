package com.namazustudios.socialengine.rest.application;

import com.namazustudios.socialengine.model.application.PSNApplicationConfiguration;
import com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource;
import com.namazustudios.socialengine.service.PSNApplicationConfigurationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Handles the management of {@link PSNApplicationConfiguration} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
@Api(value = "PSN Application Configurations",
    description = "Operations for the management of ApplictionConfigurations for the Playstation Network.",
    authorizations = {@Authorization(EnhancedApiListingResource.FACBOOK_OAUTH_KEY)})
@Path("application/{applicationNameOrId}/configuration/psn")
public class PSNApplicationConfigurationResource {

    private PSNApplicationConfigurationService psnApplicationConfigurationService;

    /**
     * Gets the specific {@link PSNApplicationConfiguration} instances assocated with the
     * application.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationConfigurationNameOrId the application profile name or ID
     *
     * @return the {@link PSNApplicationConfiguration} instance
     */
    @GET
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a PSN Application Configuration",
                  notes = "Gets a single PSN application based on unique name or ID.")
    public PSNApplicationConfiguration getPSNApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        return getPsnApplicationConfigurationService().getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    /**
     * Creates a new {@link PSNApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param psnApplicationConfiguration the PSN appliation profile object to creates
     *
     * @return the {@link PSNApplicationConfiguration} the PSN Application Configuration
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new PSN ApplicationConfiguration",
                  notes = "Creates a new PSN ApplicationConfiguration with the specific ID or application.")
    public PSNApplicationConfiguration createPSNApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final PSNApplicationConfiguration psnApplicationConfiguration) {
        return getPsnApplicationConfigurationService().createApplicationConfiguration(applicationNameOrId, psnApplicationConfiguration);
    }

    /**
     * Updates an existing {@link PSNApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param applicationConfigurationNameOrId the name or identifier of the {@link PSNApplicationConfiguration}
     * @param psnApplicationConfiguration the PSN appliation profile object to update
     *
     * @return the {@link PSNApplicationConfiguration} the PSN Application Configuration
     */
    @PUT
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a PSN ApplicationConfiguration",
                  notes = "Updates an existing PSN Application profile if it is known to the server.")
    public PSNApplicationConfiguration updateApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId,
            final PSNApplicationConfiguration psnApplicationConfiguration) {
        return getPsnApplicationConfigurationService().updateApplicationConfiguration(
                applicationNameOrId,
                applicationConfigurationNameOrId,
                psnApplicationConfiguration);
    }

    /**
     * Deletes an instance of {@link PSNApplicationConfiguration}.
     *
     * @param applicationNameOrId the application ID, or name
     * @param applicationConfigurationNameOrId the application profile ID, or name
     */
    @DELETE
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a PSN ApplicationConfiguration",
                  notes = "Deletes an existing PSN Application profile if it is known to the server.")
    public void deletePSNApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        getPsnApplicationConfigurationService().deleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    public PSNApplicationConfigurationService getPsnApplicationConfigurationService() {
        return psnApplicationConfigurationService;
    }

    @Inject
    public void setPsnApplicationConfigurationService(PSNApplicationConfigurationService psnApplicationConfigurationService) {
        this.psnApplicationConfigurationService = psnApplicationConfigurationService;
    }

}
