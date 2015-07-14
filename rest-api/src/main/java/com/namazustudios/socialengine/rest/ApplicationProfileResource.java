package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationProfile;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by patricktwohig on 7/13/15.
 */
@Path("application/profile/{applicationNameOrId}/profile")
public class ApplicationProfileResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Pagination<ApplicationProfile> getApplicationProfiles(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @QueryParam("offset") @DefaultValue("0")  final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") @DefaultValue("")   final String search) {
        return null;
    }

}
