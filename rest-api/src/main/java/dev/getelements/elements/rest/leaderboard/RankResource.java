package dev.getelements.elements.rest.leaderboard;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.Tabulation;
import dev.getelements.elements.sdk.model.leaderboard.Rank;
import dev.getelements.elements.sdk.model.leaderboard.RankRow;
import dev.getelements.elements.sdk.service.leaderboard.RankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;

@Path("rank")
public class RankResource {

    private RankService rankService;

    @GET @Path("global/{leaderboardNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Gets Rank Among all Players",
               description = "Gets the current Profile's rank among all players for the particular leaderboard.")
    public Pagination<Rank> getGlobalRank(

            @QueryParam("offset") @DefaultValue("0")
            @Parameter(description = "May be negative to place the requested player in the middle of the page.")
            final int offset,

            @QueryParam("count")
            @DefaultValue("20")
            @Parameter(description = "The number of results to return in the page.")
            final int count,

            @Parameter( description = "The profile ID of the user.  If supplied this will skip ahead in the result " +
                    "set automatically allowing the player to find his or her rank.  Unlike other API methods, the " +
                    "supplied offset can be specified in reverse as a negative number allowing the user to be " +
                    "placed in the middle of the page.")
            @QueryParam("profileId") final String profileId,

            @QueryParam("leaderboardEpoch")
            @DefaultValue("0")
            @Parameter(description = "Specifies the epoch for the leaderboard. If not provided, the current epoch " +
                    "will be used by default for epochal leaderboards. This value will be ignored for all-time " +
                    "leaderboards. Set this value to 0 to explicitly reference the current epoch (when applicable).")
            final long leaderboardEpoch,

            @PathParam("leaderboardNameOrId")
            @Parameter(description = "Specifies the leaderboard name or ID.")
            final String leaderboardNameOrId) {

        if (count < 0) {
            throw new InvalidParameterException("Count must have a non-negative value.");
        }

        if (leaderboardEpoch < 0) {
            throw new InvalidParameterException("LeaderboardEpoch must have a non-negative value.");
        }

        final String profileIdTrimmed = nullToEmpty(profileId).trim();
        final boolean relative = !profileIdTrimmed.isEmpty();

        if (!relative && offset < 0) {
            throw new InvalidParameterException("Offset must have non-negative value when using non-relative offset.");
        }

        return !relative
            ? getRankService().getRanksForGlobal(leaderboardNameOrId, offset, count, leaderboardEpoch)
            : getRankService().getRanksForGlobalRelative(leaderboardNameOrId, profileIdTrimmed, offset, count, leaderboardEpoch);

    }

    @GET
    @Path("global/{leaderboardNameOrId}")
    @Produces("text/csv")
    @Operation(
            summary = "Gets Rank Among all Players",
            description = "Gets the current Profile's rank among all players for the particular leaderboard.")
    public Tabulation<RankRow> getGlobalRankTabular(
            @PathParam("leaderboardNameOrId")
            @Parameter(description = "Specifies the leaderboard name or ID.")
            final String leaderboardNameOrId,

            @QueryParam("leaderboardEpoch")
            @DefaultValue("0")
            @Parameter(description = "Specifies the epoch for the leaderboard. If not provided, the current epoch will be used by " +
                      "default for epochal leaderboards. This value will be ignored for all-time leaderboards. Set " +
                      "this value to 0 to explicitly reference the current epoch (when applicable).")
            final long leaderboardEpoch
            ) {
        return getRankService().getRanksForGlobalTabular(leaderboardNameOrId, leaderboardEpoch);
    }

    @GET @Path("friends/{leaderboardNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets Rank among Friends",
            description = "Gets the current Profile's rank among friends for the particular leaderboard.")
    public Pagination<Rank> getRankAmongFriends(

            @QueryParam("offset")
            @DefaultValue("0")
            @Parameter(description = "May be negative to place the requested player in the middle of the page.")
            final int offset,

            @QueryParam("count")
            @DefaultValue("20")
            @Parameter(description = "The number of results to return in the page.")
            final int count,


            @QueryParam("relative")
            @DefaultValue("false")
            @Parameter(description = "Indicates whether or not to fetch results in a relative fashion.")
            final boolean relative,

            @QueryParam("leaderboardEpoch")
            @DefaultValue("0")
            @Parameter(description = "Specifies the epoch for the leaderboard. If no value is provided, the current epoch will be" +
                    "fetched.")
            final long leaderboardEpoch,

            @PathParam("leaderboardNameOrId")
            @Parameter(description = "Specifies the leaderboard name or ID.")
            final String leaderboardNameOrId) {

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        if (!relative && offset < 0) {
            throw new InvalidParameterException("Offset must have positive value when using non-relative offset.");
        }

        if (leaderboardEpoch < 0) {
            throw new InvalidParameterException("LeaderboardEpoch must have positive value.");
        }

        return !relative ?
                getRankService().getRanksForFriends(leaderboardNameOrId, offset, count, leaderboardEpoch) :
                getRankService().getRanksForFriendsRelative(leaderboardNameOrId, offset, count, leaderboardEpoch);

    }

    @GET
    @Path("mutual_followers/{leaderboardNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets Rank among Mutual Followers",
            description = "Gets the current Profile's rank among mutual followers for the particular leaderboard.")
    public Pagination<Rank> getRankAmongMutualFollowers(

            @QueryParam("offset")
            @DefaultValue("0")
            @Parameter(description = "May be negative to place the requested player in the middle of the page.")
            final int offset,

            @QueryParam("count")
            @DefaultValue("20")
            @Parameter(description = "The number of results to return in the page.")
            final int count,

            @QueryParam("relative")
            @DefaultValue("false")
            @Parameter(description = "Indicates whether or not to fetch results in a relative fashion.")
            final boolean relative,

            @QueryParam("leaderboardEpoch")
            @DefaultValue("0")
            @Parameter(description = "Specifies the epoch for the leaderboard. If no value is provided, the current epoch will be" +
                    "fetched.")
            final long leaderboardEpoch,

            @PathParam("leaderboardNameOrId")
            @Parameter(description = "Specifies the leaderboard name or ID.")
            final String leaderboardNameOrId) {

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        if (!relative && offset < 0) {
            throw new InvalidParameterException("Offset must have positive value when using non-relative offset.");
        }

        if (leaderboardEpoch < 0) {
            throw new InvalidParameterException("LeaderboardEpoch must have positive value.");
        }

        return !relative ?
                getRankService().getRanksForMutualFollowers(leaderboardNameOrId, offset, count, leaderboardEpoch) :
                getRankService().getRanksForMutualFollowersRelative(leaderboardNameOrId, offset, count, leaderboardEpoch);

    }

    public RankService getRankService() {
        return rankService;
    }

    @Inject
    public void setRankService(RankService rankService) {
        this.rankService = rankService;
    }

}
