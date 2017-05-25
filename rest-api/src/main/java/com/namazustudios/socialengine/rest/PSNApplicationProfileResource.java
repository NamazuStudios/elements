package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.application.PSNApplicationProfile;
import com.namazustudios.socialengine.service.PSNApplicationProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Handles the creation of {@link PSNApplicationProfile} isntances.
 *
 * Created by patricktwohig on 7/13/15.
 */
@Api(value = "PSN Application Profiles",
    description = "Operations for the management of ApplictionProfiles for the Playstation Network.")
@Path("application/{applicationNameOrId}/profile/psn")
public class PSNApplicationProfileResource {

    private PSNApplicationProfileService psnApplicationProfileService;

    /**
     * Gets the specific {@link PSNApplicationProfile} instances assocated with the
     * application.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationProfileNameOrId the application profile name or ID
     *
     * @return the {@link PSNApplicationProfile} instance
     */
    @GET
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a PSN Application Profile",
                  notes = "Gets a single PSN application based on unique name or ID.")
    public PSNApplicationProfile getPSNApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId) {
        return getPsnApplicationProfileService().getPSNApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    /**
     * Creates a new {@link PSNApplicationProfile} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param psnApplicationProfile the PSN appliation profile object to creates
     *
     * @return the {@link PSNApplicationProfile} the PSN Application Profile
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new PSN ApplicationProfile",
                  notes = "Creates a new PSN ApplicationProfile with the specific ID or application.")
    public PSNApplicationProfile createPSNApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final PSNApplicationProfile psnApplicationProfile) {
        return getPsnApplicationProfileService().createApplicationProfile(applicationNameOrId, psnApplicationProfile);
    }

    /**
     * Updates an existing {@link PSNApplicationProfile} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param applicationProfileNameOrId the name or identifier of the {@link PSNApplicationProfile}
     * @param psnApplicationProfile the PSN appliation profile object to update
     *
     * @return the {@link PSNApplicationProfile} the PSN Application Profile
     */
    @PUT
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a PSN ApplicationProfile",
                  notes = "Updates an existing PSN Application profile if it is known to the server.")
    public PSNApplicationProfile updateApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId,
            final PSNApplicationProfile psnApplicationProfile) {
        return getPsnApplicationProfileService().updateApplicationProfile(
                applicationNameOrId,
                applicationProfileNameOrId,
                psnApplicationProfile);
    }

    /**
     * Deletes an instance of {@link PSNApplicationProfile}.
     *
     * @param applicationNameOrId the application ID, or name
     * @param applicationProfileNameOrId the application profile ID, or name
     */
    @DELETE
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a PSN ApplicationProfile",
                  notes = "Deletes an existing PSN Application profile if it is known to the server.")
    public void deletePSNApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId) {
        getPsnApplicationProfileService().deleteApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    public PSNApplicationProfileService getPsnApplicationProfileService() {
        return psnApplicationProfileService;
    }

    @Inject
    public void setPsnApplicationProfileService(PSNApplicationProfileService psnApplicationProfileService) {
        this.psnApplicationProfileService = psnApplicationProfileService;
    }

}
