package dev.getelements.elements.rest.leaderboard;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.leaderboard.CreateLeaderboardRequest;
import dev.getelements.elements.sdk.model.leaderboard.Leaderboard;
import dev.getelements.elements.sdk.model.leaderboard.UpdateLeaderboardRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.leaderboard.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;

@Path("leaderboard")
public class LeaderboardResource {

    private LeaderboardService leaderboardService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Search Leaderboards",
            description = "Performs a full-text search of all leaderboards known to the server.  As with " +
                          "other full-text endpoints this allows for pagination and offset.")
    public Pagination<Leaderboard> getLeaderboards(@QueryParam("offset") @DefaultValue("0")  final int offset,
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
    @Operation(
            summary = "Get an Leaderboard",
            description = "Gets the metadata for a single leaderboard.  This may include more specific " +
                    "details not availble in the bulk-get or fetch operation.")
    public Leaderboard getLeaderboard(@PathParam("nameOrId") final String nameOrId) {

        return getLeaderboardService().getLeaderboard(nameOrId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a New Leaderboard",
            description = "Gets the metadata for a single leaderboard.  This may include more specific " +
                    "details not available in the bulk-get or fetch operation.")
    public Leaderboard createLeaderboard(final CreateLeaderboardRequest leaderboard) {

        getValidationHelper().validateModel(leaderboard);

        return getLeaderboardService().createLeaderboard(leaderboard);
    }

    @PUT
    @Path("{nameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates an Leaderboard",
            description = "Performs an update to an existing leaderboard known to the server.")
    public Leaderboard updateLeaderboard(@PathParam("nameOrId") final String nameOrId,
                                         final UpdateLeaderboardRequest leaderboard) {

        getValidationHelper().validateModel(leaderboard);

        return getLeaderboardService().updateLeaderboard(nameOrId, leaderboard);
    }

    @DELETE
    @Path("{nameOrId}")
    @Operation(
            summary = "Deletes an Leaderboard",
            description = "Deletes a specific leaderboard known to the server.")
    public void deleteLeaderboard(@PathParam("nameOrId") final String nameOrId) {

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
