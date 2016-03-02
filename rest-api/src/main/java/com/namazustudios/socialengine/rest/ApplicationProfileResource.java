package com.namazustudios.socialengine.rest;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.service.ApplicationProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by patricktwohig on 7/13/15.
 */
@Api(value = "Application Profiles",
     description = "Manages application profiles.  An application profile is a collection of " +
                   "application metadata for a particular configuration of deployment.  For example, " +
                   "an application may be deployed on both Android and iOS.  One application profile" +
                   "each for Android and iOS would be required.")
@Path("application/{applicationNameOrId}/profile")
public class ApplicationProfileResource {

    @Inject
    private ApplicationProfileService applicationProfileService;

    @GET
    @Path("{applicationNameOrId}/{offset}/{count}/{search}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Searches application profiles",
                  notes = "Searches all instances of ApplicationProfiles associated with " +
                          "the application.  The search query may be a full text search.")
    public Pagination<? extends ApplicationProfile> searchApplicationProfiles(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("offset") final int offset,
            @PathParam("count")  final int count,
            @PathParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return applicationProfileService.getApplicationProfiles(applicationNameOrId, offset, count, search);

    }

    /**
     * Searches for all instances of {@link ApplicationProfile} associated with the
     * application.
     *
     * @param applicationNameOrId the application name or identifier
     * @param offset the offset
     * @param count the count
     * @return the {@link Pagination} of {@link ApplicationProfile} instances
     */
    @GET
    @Path("{applicationNameOrId}/{offset}/{count}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets application profiles",
                  notes = "Lists all instances of ApplicationProfiles associated with " +
                          "the application.")
    public Pagination<? extends ApplicationProfile> getApplicationProfiles(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("offset") @DefaultValue("0")  final int offset,
            @PathParam("count")  @DefaultValue("20") final int count) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return applicationProfileService.getApplicationProfiles(applicationNameOrId, offset, count);

    }

}
