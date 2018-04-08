package com.namazustudios.socialengine.rest.leaderboard;

import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Leaderboard;
import com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource;
import com.namazustudios.socialengine.service.LeaderboardService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;

@Api(value = "Leaderboards",
     description = "Allows for the manipulation of the leaderboard metadata.  This allows admin users to implement new" +
                   "add/remove or disable leaderboards in the system.   Regular users may have limited access to read " +
                   "leaderboard metadata.",
        authorizations = {@Authorization(EnhancedApiListingResource.SESSION_SECRET)})
@Path("leaderboard")
public class LeaderboardResource {

    private LeaderboardService leaderboardService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search Leaderboards",
            notes = "Performs a full-text search of all leaderboards known to the server.  As with " +
                    "other full-text endpoints this allows for pagination and offset.")
    public Pagination<Leaderboard> getLeaderboards(
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
            getLeaderboardService().getLeaderboards(offset, count) :
            getLeaderboardService().getLeaderboards(offset, count, query);

    }

    @GET
    @Path("{nameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get an Leaderboard",
            notes = "Gets the metadata for a single leaderboard.  This may include more specific " +
                    "details not availble in the bulk-get or fetch operation.")
    public Leaderboard getLeaderboard(@PathParam("nameOrId") final String nameOrId) {
        return getLeaderboardService().getLeaderboard(nameOrId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a New Leaderboard",
            notes = "Gets the metadata for a single leaderboard.  This may include more specific " +
                    "details not available in the bulk-get or fetch operation.")
    public Leaderboard createLeaderboard(final Leaderboard leaderboard) {
        getValidationHelper().validateModel(leaderboard);
        return getLeaderboardService().createLeaderboard(leaderboard);
    }

    @PUT
    @Path("{nameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates an Leaderboard",
            notes = "Performs an update to an existing leaderboard known to the server.")
    public Leaderboard updateLeaderboard(
            @PathParam("nameOrId") final String nameOrId,
            final Leaderboard leaderboard) {
        getValidationHelper().validateModel(leaderboard);
        return getLeaderboardService().updateLeaderboard(nameOrId, leaderboard);
    }

    @DELETE
    @Path("{nameOrId}")
    @ApiOperation(value = "Deletes an Leaderboard",
            notes = "Deletes a specific leaderboard known to the server.")
    public void deleteLeaderboard(
            @PathParam("nameOrId") final String nameOrId) {
        getLeaderboardService().deleteLeaderboard(nameOrId);
    }

    public LeaderboardService getLeaderboardService() {
        return leaderboardService;
    }

    @Inject
    public void setLeaderboardService(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
