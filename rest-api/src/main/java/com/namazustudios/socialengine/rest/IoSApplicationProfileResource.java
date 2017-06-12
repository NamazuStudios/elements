package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.application.IosApplicationProfile;
import com.namazustudios.socialengine.service.IosApplicationProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Handles the management of {@link IosApplicationProfile} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
@Api(
    value = "iOS Application Profiles",
    description = "Operations for the management of ApplictionProfiles for iOS Applications.")
@Path("application/{applicationNameOrId}/profile/ios")
public class IoSApplicationProfileResource {

    private IosApplicationProfileService iosApplicationProfileService;

    /**
     * Gets the specific {@link IosApplicationProfile} instances assocated with the
     * application.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationProfileNameOrId the application profile name or ID
     *
     * @return the {@link IosApplicationProfile} instance
     */
    @GET
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Gets a iOS Application Profile",
        notes = "Gets a single iOS application based on unique name or ID.")
    public IosApplicationProfile getIosApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId) {
        return getIosApplicationProfileService().getApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    /**
     * Creates a new {@link IosApplicationProfile} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param iosApplicationProfiles the iOS appliation profile object to creates
     *
     * @return the {@link IosApplicationProfile} the iOS Application Profile
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Creates a new iOS ApplicationProfile",
        notes = "Creates a new iOS ApplicationProfile with the specific ID or application.")
    public IosApplicationProfile createIosApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final IosApplicationProfile iosApplicationProfiles) {
        return getIosApplicationProfileService().createApplicationProfile(applicationNameOrId, iosApplicationProfiles);
    }

    /**
     * Updates an existing {@link IosApplicationProfile} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param applicationProfileNameOrId the name or identifier of the {@link IosApplicationProfile}
     * @param iosApplicationProfile the iOS application profile object to update
     *
     * @return the {@link IosApplicationProfile} the iOS Application Profile
     */
    @PUT
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Updates a iOS ApplicationProfile",
            notes = "Updates an existing iOS Application profile if it is known to the server.")
    public IosApplicationProfile updateApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId,
            final IosApplicationProfile iosApplicationProfile) {
        return getIosApplicationProfileService().updateApplicationProfile(
                applicationNameOrId,
                applicationProfileNameOrId,
                iosApplicationProfile);
    }

    /**
     * Deletes an instance of {@link IosApplicationProfile}.
     *
     * @param applicationNameOrId the application ID, or name
     * @param applicationProfileNameOrId the application profile ID, or name
     */
    @DELETE
    @Path("{applicationProfileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Deletes a iOS ApplicationProfile",
            notes = "Deletes an existing iOS Application profile if it is known to the server.")
    public void deleteIosApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationProfileNameOrId") final String applicationProfileNameOrId) {
        getIosApplicationProfileService().deleteApplicationProfile(applicationNameOrId, applicationProfileNameOrId);
    }

    public IosApplicationProfileService getIosApplicationProfileService() {
        return iosApplicationProfileService;
    }

    @Inject
    public void setIosApplicationProfileService(IosApplicationProfileService iosApplicationProfileService) {
        this.iosApplicationProfileService = iosApplicationProfileService;
    }

}
