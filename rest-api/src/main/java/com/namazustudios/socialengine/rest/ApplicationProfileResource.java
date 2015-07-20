package com.namazustudios.socialengine.rest;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import com.namazustudios.socialengine.service.ApplicationProfileService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by patricktwohig on 7/13/15.
 */
@Path("application/{applicationNameOrId}/profile")
public class ApplicationProfileResource {

    @Inject
    private ApplicationProfileService applicationProfileService;

    /**
     * Searches for all instances of {@link ApplicationProfile} associated with the
     * application.
     *
     * @param applicationNameOrId the application name or identifier
     * @param offset the offset
     * @param count the count
     * @param search the search query
     * @return the {@link Pagination} of {@link ApplicationProfile} instances
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Pagination<? extends ApplicationProfile> getApplicationProfiles(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @QueryParam("offset") @DefaultValue("0")  final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") @DefaultValue("")   final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return Strings.nullToEmpty(search).trim().isEmpty() ?
                applicationProfileService.getApplicationProfiles(applicationNameOrId, offset, count) :
                applicationProfileService.getApplicationProfiles(applicationNameOrId, offset, count, search);

    }

}
