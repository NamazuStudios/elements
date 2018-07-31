package com.namazustudios.socialengine.rest.gameon;

import com.namazustudios.socialengine.model.gameon.*;
import com.namazustudios.socialengine.service.GameOnMatchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

@Api(value = "GameOn Matches",
        description = "Provides access to the current profile's game on matches.  In the purview of the GameOn APIs, " +
                      "match refers those tracked by the GameOn system and not the Matches managed by Elements.",
        authorizations = {@Authorization(SESSION_SECRET)})
@Path("game_on/match")
public class GameOnMatchResource {

    private GameOnMatchService gameOnMatchService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Gets all matches.",
            notes = "")
    public GameOnMatchesAggregate getMatches(

            @QueryParam("os")
            @DefaultValue(DeviceOSType.DEFAULT_TYPE_STRING)
            final DeviceOSType deviceOSType,

            @QueryParam("build")
            @DefaultValue(AppBuildType.DEFAULT_TYPE_STRING)
            final AppBuildType appBuildType,

            @QueryParam("filterBy")
            final MatchFilter filterBy,

            @QueryParam("matchType")
            final MatchType matchType,

            @QueryParam("period")
            final TournamentPeriod period,

            @QueryParam("playerAttributes")
            @ApiParam("Custom player attributes.")
            final String playerAttributes

    ) {
        return getGameOnMatchService().getMatches(
            deviceOSType, appBuildType,                     // Session related parameters
            filterBy, matchType, period, playerAttributes); // Filter/query related parameters
    }

    @GET
    @Path("{matchId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a single available tournament.",
            notes = "Gets a single available tournament, specified by the identifier.")
    public GameOnTournamentDetail getTournament(

            @PathParam("matchId")
            @ApiParam("The match ID.")
            final String matchId,

            @QueryParam("os")
            @DefaultValue(DeviceOSType.DEFAULT_TYPE_STRING)
            final DeviceOSType deviceOSType,

            @QueryParam("build")
            @DefaultValue(AppBuildType.DEFAULT_TYPE_STRING)
            final AppBuildType appBuildType,

            @QueryParam("playerAttributes")
            @ApiParam("Custom player attributes.")
            final String playerAttributes

    ) {
        return getGameOnMatchService().getMatch(
                deviceOSType, appBuildType,       // Session related parameters
                playerAttributes, matchId);  // Filer/query related parameters
    }

    public GameOnMatchService getGameOnMatchService() {
        return gameOnMatchService;
    }

    @Inject
    public void setGameOnMatchService(GameOnMatchService gameOnMatchService) {
        this.gameOnMatchService = gameOnMatchService;
    }

}
