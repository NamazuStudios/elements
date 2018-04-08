package com.namazustudios.socialengine.rest.leaderboard;

import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Rank;
import com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource;
import com.namazustudios.socialengine.service.RankService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;

@Api(value = "Ranking",
     description = "Fetches ranks for leaderboards.",
     authorizations = {@Authorization(EnhancedApiListingResource.SESSION_SECRET)})
@Path("rank")
public class RankResource {

    private RankService rankService;

    @GET @Path("friends/{leaderboardNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets Rank among Friends",
                  notes = "Gets the current Profile's rank among friends for the particular leaderboard.")
    public Pagination<Rank> getRankAmongFriends(

            @QueryParam("offset") @DefaultValue("0")
            @ApiParam("May be negative to place the requested player in the middle of the page.")
            final int offset,

            @QueryParam("count")
            @DefaultValue("20")
            @ApiParam("The number of results to return in the page.")
            final int count,

            @ApiParam("The profile ID of the user.  If supplied this will skip ahead in the result set automatically " +
                      "allowing the player to find his or her rank.  Unlike other API methods, the supplied offset " +
                      "can be specified in reverse as a negative number allowing the user to be placed in the middle " +
                      "of the page.")
            @QueryParam("profileId") final String profileId,

            @QueryParam("leaderboardNameOrId")
            @ApiParam("Specifies the leaderboard name or ID.")
            final String leaderboardNameOrId) {

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final String profileIdTrimmed = nullToEmpty(profileId).trim();

        return profileIdTrimmed.isEmpty() ?
                getRankService().getRanksForFriends(offset, count) :
                getRankService().getRanksForFriends(offset, count, profileIdTrimmed);

    }

    @GET @Path("friends/{leaderboardNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets Rank Among all Players",
                  notes = "Gets the current Profile's rank among all players for the particular leaderboard.")
    public Pagination<Rank> getGlobalRank(

            @QueryParam("offset") @DefaultValue("0")
            @ApiParam("May be negative to place the requested player in the middle of the page.")
            final int offset,

            @QueryParam("count")
            @DefaultValue("20")
            @ApiParam("The number of results to return in the page.")
            final int count,

            @ApiParam("The profile ID of the user.  If supplied this will skip ahead in the result set automatically " +
                      "allowing the player to find his or her rank.  Unlike other API methods, the supplied offset " +
                      "can be specified in reverse as a negative number allowing the user to be placed in the middle " +
                      "of the page.")
            @QueryParam("profileId")
            final String profileId,

            @QueryParam("leaderboardNameOrId")
            @ApiParam("Specifies the leaderboard name or ID.")
            final String leaderboardNameOrId) {

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final String profileIdTrimmed = nullToEmpty(profileId).trim();

        return profileIdTrimmed.isEmpty() ?
            getRankService().getRanksForGlobal(offset, count) :
            getRankService().getRanksForGlobal(offset, count, profileIdTrimmed);

    }

    public RankService getRankService() {
        return rankService;
    }

    @Inject
    public void setRankService(RankService rankService) {
        this.rankService = rankService;
    }

}
