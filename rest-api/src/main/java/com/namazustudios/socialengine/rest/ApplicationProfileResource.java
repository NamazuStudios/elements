package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.service.ApplicationProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;

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

    private ApplicationProfileService applicationProfileService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Searches application profiles",
                  notes = "Searches all instances of ApplicationProfiles associated with " +
                          "the application.  The search query may be a full text search.")
    public Pagination<ApplicationProfile> getApplicationProfiles(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @QueryParam("offset") @DefaultValue("0")  final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final String query = nullToEmpty(search).trim();

        return query.isEmpty() ?
            getApplicationProfileService().getApplicationProfiles(applicationNameOrId, offset, count) :
            getApplicationProfileService().getApplicationProfiles(applicationNameOrId, offset, count, search);

    }

    public ApplicationProfileService getApplicationProfileService() {
        return applicationProfileService;
    }

    @Inject
    public void setApplicationProfileService(ApplicationProfileService applicationProfileService) {
        this.applicationProfileService = applicationProfileService;
    }

}
