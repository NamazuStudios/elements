package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.application.PSNApplicationProfile;
import com.namazustudios.socialengine.service.ApplicationProfileService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Handles the creation of {@link PSNApplicationProfile} isntances.
 *
 * Created by patricktwohig on 7/13/15.
 */
@Path("application/profile/{applicationNameOrId}/profile/psn")
public class PSNApplicationProfileResource {

    @Inject
    private ApplicationProfileService applicationProfileService;

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
    public PSNApplicationProfile getApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId) {
        return applicationProfileService.getApplicationProfile(applicationNameOrId,
                applicationProfileNameOrId, PSNApplicationProfile.class);
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
    public PSNApplicationProfile createApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final PSNApplicationProfile psnApplicationProfile) {
        return applicationProfileService.createApplicationProfile(applicationNameOrId, psnApplicationProfile);
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
    public PSNApplicationProfile updateApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId,
            final PSNApplicationProfile psnApplicationProfile) {
        return applicationProfileService.updateApplicationProfile(applicationNameOrId,
                applicationProfileNameOrId, psnApplicationProfile);
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
    public void deleteApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId) {
        applicationProfileService.deleteApplicationProfile(applicationNameOrId,
                applicationProfileNameOrId, PSNApplicationProfile.class);
    }

}
