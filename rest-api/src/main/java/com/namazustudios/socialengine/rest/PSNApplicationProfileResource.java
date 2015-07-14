package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.model.application.PSNApplicationProfile;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Handles the creation of {@link }
 * Created by patricktwohig on 7/13/15.
 */
@Path("application/profile/{applicationNameOrId}/profile/psn")
public class PSNApplicationProfileResource {

    @GET
    @Path("{profileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApplicationProfile getApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("profileNameOrId") final String profileNameOrId) {
        return null;
    }

    @POST
    public PSNApplicationProfile createApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final PSNApplicationProfile psnApplicationProfile) {
        return null;
    }

    @PUT
    public PSNApplicationProfile updateApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final PSNApplicationProfile psnApplicationProfile) {
        return null;
    }

    @DELETE
    @Path("{profileNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteApplicationProfile(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("profileNameOrId") final String profileNameOrId) {

    }

}
