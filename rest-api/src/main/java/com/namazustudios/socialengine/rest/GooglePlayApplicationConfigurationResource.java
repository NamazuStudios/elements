package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.application.GooglePlayApplicationConfiguration;
import com.namazustudios.socialengine.service.GooglePlayApplicationConfigurationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Handles the management of {@link GooglePlayApplicationConfiguration} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
@Api(value = "Google Play Application Configuration",
     description = "Operations for the management of ApplictionProfiles for Google Play.")
@Path("application/{applicationNameOrId}/configuration/google_play")
public class GooglePlayApplicationConfigurationResource {

    private GooglePlayApplicationConfigurationService googlePlayApplicationConfigurationService;

    /**
     * Gets the specific {@link GooglePlayApplicationConfiguration} instances assocated with the
     * application.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationProfileNameOrId the application profile name or ID
     *
     * @return the {@link GooglePlayApplicationConfiguration} instance
     */
    @GET
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Gets a Google Play Application Profile",
            notes = "Gets a single Google Play application based on unique name or ID.")
    public GooglePlayApplicationConfiguration getGooglePlayApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId) {
        return getGooglePlayApplicationConfigurationService().getApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    /**
     * Creates a new {@link GooglePlayApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param googlePlayApplicationProfile the Google Play appliation profile object to creates
     *
     * @return the {@link GooglePlayApplicationConfiguration} the Google Play Application Profile
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a new Google Play ApplicationConfiguration",
            notes = "Creates a new GooglePlay ApplicationConfiguration with the specific ID or application.")
    public GooglePlayApplicationConfiguration createGooglePlayApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final GooglePlayApplicationConfiguration googlePlayApplicationProfile) {
        return getGooglePlayApplicationConfigurationService().createApplicationProfile(applicationNameOrId, googlePlayApplicationProfile);
    }

    /**
     * Updates an existing {@link GooglePlayApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param applicationProfileNameOrId the name or identifier of the {@link GooglePlayApplicationConfiguration}
     * @param googlePlayApplicationProfile the Google Play application profile object to update
     *
     * @return the {@link GooglePlayApplicationConfiguration} the Google Play Application Profile
     */
    @PUT
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Updates a Google Play ApplicationConfiguration",
            notes = "Updates an existing Google Play Application profile if it is known to the server.")
    public GooglePlayApplicationConfiguration updateApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId,
            final GooglePlayApplicationConfiguration googlePlayApplicationProfile) {
        return getGooglePlayApplicationConfigurationService().updateApplicationProfile(
                applicationNameOrId,
                applicationProfileNameOrId,
                googlePlayApplicationProfile);
    }

    /**
     * Deletes an instance of {@link GooglePlayApplicationConfiguration}.
     *
     * @param applicationNameOrId the application ID, or name
     * @param applicationProfileNameOrId the application profile ID, or name
     */
    @DELETE
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Deletes a Google Play ApplicationConfiguration",
            notes = "Deletes an existing Google Play Application profile if it is known to the server.")
    public void deleteGooglePlayApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId) {
        getGooglePlayApplicationConfigurationService().deleteApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }


    public GooglePlayApplicationConfigurationService getGooglePlayApplicationConfigurationService() {
        return googlePlayApplicationConfigurationService;
    }

    @Inject
    public void setGooglePlayApplicationConfigurationService(GooglePlayApplicationConfigurationService googlePlayApplicationConfigurationService) {
        this.googlePlayApplicationConfigurationService = googlePlayApplicationConfigurationService;
    }

}
