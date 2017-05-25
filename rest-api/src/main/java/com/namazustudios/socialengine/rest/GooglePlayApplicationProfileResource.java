package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.application.GooglePlayApplicationProfile;
import com.namazustudios.socialengine.service.GooglePlayApplicationProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Handles the management of {@link GooglePlayApplicationProfile} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
@Api(value = "Google Play Application Profiles",
     description = "Operations for the management of ApplictionProfiles for Google Play.")
@Path("application/{applicationNameOrId}/profile/google_play")
public class GooglePlayApplicationProfileResource {

    private GooglePlayApplicationProfileService googlePlayApplicationProfileService;

    /**
     * Gets the specific {@link GooglePlayApplicationProfile} instances assocated with the
     * application.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationProfileNameOrId the application profile name or ID
     *
     * @return the {@link GooglePlayApplicationProfile} instance
     */
    @GET
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Gets a Google Play Application Profile",
            notes = "Gets a single Google Play application based on unique name or ID.")
    public GooglePlayApplicationProfile getGooglePlayApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId) {
        return getGooglePlayApplicationProfileService().getApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    /**
     * Creates a new {@link GooglePlayApplicationProfile} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param googlePlayApplicationProfile the Google Play appliation profile object to creates
     *
     * @return the {@link GooglePlayApplicationProfile} the Google Play Application Profile
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a new Google Play ApplicationProfile",
            notes = "Creates a new GooglePlay ApplicationProfile with the specific ID or application.")
    public GooglePlayApplicationProfile createGooglePlayApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final GooglePlayApplicationProfile googlePlayApplicationProfile) {
        return getGooglePlayApplicationProfileService().createApplicationProfile(applicationNameOrId, googlePlayApplicationProfile);
    }

    /**
     * Updates an existing {@link GooglePlayApplicationProfile} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param applicationProfileNameOrId the name or identifier of the {@link GooglePlayApplicationProfile}
     * @param googlePlayApplicationProfile the Google Play application profile object to update
     *
     * @return the {@link GooglePlayApplicationProfile} the Google Play Application Profile
     */
    @PUT
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Updates a Google Play ApplicationProfile",
            notes = "Updates an existing Google Play Application profile if it is known to the server.")
    public GooglePlayApplicationProfile updateApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId,
            final GooglePlayApplicationProfile googlePlayApplicationProfile) {
        return getGooglePlayApplicationProfileService().updateApplicationProfile(
                applicationNameOrId,
                applicationProfileNameOrId,
                googlePlayApplicationProfile);
    }

    /**
     * Deletes an instance of {@link GooglePlayApplicationProfile}.
     *
     * @param applicationNameOrId the application ID, or name
     * @param applicationProfileNameOrId the application profile ID, or name
     */
    @DELETE
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Deletes a Google Play ApplicationProfile",
            notes = "Deletes an existing Google Play Application profile if it is known to the server.")
    public void deleteGooglePlayApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId) {
        getGooglePlayApplicationProfileService().deleteApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }


    public GooglePlayApplicationProfileService getGooglePlayApplicationProfileService() {
        return googlePlayApplicationProfileService;
    }

    @Inject
    public void setGooglePlayApplicationProfileService(GooglePlayApplicationProfileService googlePlayApplicationProfileService) {
        this.googlePlayApplicationProfileService = googlePlayApplicationProfileService;
    }

}
