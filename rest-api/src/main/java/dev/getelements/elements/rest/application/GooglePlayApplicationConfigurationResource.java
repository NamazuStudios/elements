package dev.getelements.elements.rest.application;

import dev.getelements.elements.sdk.model.application.GooglePlayApplicationConfiguration;
import dev.getelements.elements.sdk.service.application.GooglePlayApplicationConfigurationService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * Handles the management of {@link GooglePlayApplicationConfiguration} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
@Path("application/{applicationNameOrId}/configuration/google_play")
public class GooglePlayApplicationConfigurationResource {

    private GooglePlayApplicationConfigurationService googlePlayApplicationConfigurationService;

    /**
     * Gets the specific {@link GooglePlayApplicationConfiguration} instances assocated with the
     * application.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationConfigurationNameOrId the application profile name or ID
     *
     * @return the {@link GooglePlayApplicationConfiguration} instance
     */
    @GET
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a Google Play Application Configuration",
            description = "Gets a single Google Play application based on unique name or ID.")
    public GooglePlayApplicationConfiguration getGooglePlayApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        return getGooglePlayApplicationConfigurationService().getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    /**
     * Creates a new {@link GooglePlayApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param googlePlayApplicationConfiguration the Google Play appliation profile object to creates
     *
     * @return the {@link GooglePlayApplicationConfiguration} the Google Play Application Configuration
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new Google Play ApplicationConfiguration",
            description = "Creates a new GooglePlay ApplicationConfiguration with the specific ID or application.")
    public GooglePlayApplicationConfiguration createGooglePlayApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {
        return getGooglePlayApplicationConfigurationService().createApplicationConfiguration(applicationNameOrId, googlePlayApplicationConfiguration);
    }

    /**
     * Updates an existing {@link GooglePlayApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param applicationConfigurationNameOrId the name or identifier of the {@link GooglePlayApplicationConfiguration}
     * @param googlePlayApplicationConfiguration the Google Play application profile object to update
     *
     * @return the {@link GooglePlayApplicationConfiguration} the Google Play Application Configuration
     */
    @PUT
    @Path("{applicationConfigurationNameOrId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates a Google Play ApplicationConfiguration",
            description = "Updates an existing Google Play Application profile if it is known to the server.")
    public GooglePlayApplicationConfiguration updateGooglePlayApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId,
            final GooglePlayApplicationConfiguration googlePlayApplicationConfiguration) {
        return getGooglePlayApplicationConfigurationService().updateApplicationConfiguration(
                applicationNameOrId,
                applicationConfigurationNameOrId,
                googlePlayApplicationConfiguration);
    }

    /**
     * Deletes an instance of {@link GooglePlayApplicationConfiguration}.
     *
     * @param applicationNameOrId the application ID, or name
     * @param applicationConfigurationNameOrId the application profile ID, or name
     */
    @DELETE
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Deletes a Google Play ApplicationConfiguration",
            description = "Deletes an existing Google Play Application profile if it is known to the server.")
    public void deleteGooglePlayApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        getGooglePlayApplicationConfigurationService().deleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }


    public GooglePlayApplicationConfigurationService getGooglePlayApplicationConfigurationService() {
        return googlePlayApplicationConfigurationService;
    }

    @Inject
    public void setGooglePlayApplicationConfigurationService(GooglePlayApplicationConfigurationService googlePlayApplicationConfigurationService) {
        this.googlePlayApplicationConfigurationService = googlePlayApplicationConfigurationService;
    }

}
