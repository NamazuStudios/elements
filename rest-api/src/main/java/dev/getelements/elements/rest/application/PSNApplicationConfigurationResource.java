package dev.getelements.elements.rest.application;

import dev.getelements.elements.sdk.model.application.PSNApplicationConfiguration;
import dev.getelements.elements.sdk.service.application.PSNApplicationConfigurationService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * Handles the management of {@link PSNApplicationConfiguration} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
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
    @Operation(summary = "Gets a PSN Application Configuration",
                  description = "Gets a single PSN application based on unique name or ID.")
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Creates a new PSN ApplicationConfiguration",
                  description = "Creates a new PSN ApplicationConfiguration with the specific ID or application.")
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Updates a PSN ApplicationConfiguration",
                  description = "Updates an existing PSN Application profile if it is known to the server.")
    public PSNApplicationConfiguration updatePSNApplicationConfiguration(
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
    @Operation(summary = "Deletes a PSN ApplicationConfiguration",
                  description = "Deletes an existing PSN Application profile if it is known to the server.")
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
