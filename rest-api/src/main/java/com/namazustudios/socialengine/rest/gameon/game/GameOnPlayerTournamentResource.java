package com.namazustudios.socialengine.rest.gameon.game;

import com.namazustudios.socialengine.model.gameon.game.GameOnPlayerTournamentEnterRequest;
import com.namazustudios.socialengine.model.gameon.game.GameOnPlayerTournamentEnterResponse;
import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.service.GameOnPlayerTournamentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

@Api(value = "GameOnPlayerTournament",
     description = "Provides access to the eligible tournaments.  An eligible tournament is one for which the player " +
                   "qualifies and has not already entered.",
     authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("game_on/tournament/player")
public class GameOnPlayerTournamentResource {

    private GameOnPlayerTournamentService gameOnPlayerTournamentService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Gets all available tournaments",
        notes = "Gets all availble tournaments that the player can enter.  This automatically filters out " +
                "any tournaments that the player has not alrady entered.  " +
                "See: https://developer.amazon.com/docs/gameon/game-api-ref.html#get-player-tournaments")
    public List<GameOnTournamentSummary> getTournaments(

            @QueryParam("os")
            @DefaultValue(DeviceOSType.DEFAULT_TYPE_STRING)
            final DeviceOSType deviceOSType,

            @QueryParam("build")
            @DefaultValue(AppBuildType.DEFAULT_TYPE_STRING)
            final AppBuildType appBuildType,

            @QueryParam("filterBy")
            final TournamentFilter filterBy,

            @QueryParam("limit")
            final int limit,

            @QueryParam("period")
            final TournamentPeriod period,

            @QueryParam("playerAttributes")
            @ApiParam("Custom player attributes.")
            final String playerAttributes

    ) {
        return getGameOnPlayerTournamentService().getEligibleTournaments(
                deviceOSType, appBuildType,             // Session related parameters
                filterBy, limit, period, playerAttributes);    // Filter/query related parameters
    }

    @GET
    @Path("{tournamentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a single available tournament.",
                  notes = "Gets a single available tournament, specified by the identifier.  This will return 404 if " +
                          "the player is not eligible to because they have already entered.  " +
                          "See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#get-player-tournament-details")
    public GameOnTournamentDetail getTournament(

            @PathParam("tournamentId")
            @ApiParam("The player tournamet ID.")
            final String tournamentId,

            @QueryParam("os")
            @DefaultValue(DeviceOSType.DEFAULT_TYPE_STRING)
            final DeviceOSType deviceOSType,

            @QueryParam("build")
            @DefaultValue(AppBuildType.DEFAULT_TYPE_STRING)
            final AppBuildType appBuildType,

            @QueryParam("filterBy")
            final TournamentFilter filterBy,

            @QueryParam("playerAttributes")
            @ApiParam("Custom player attributes.")
            final String playerAttributes

    ) {
        return getGameOnPlayerTournamentService().getEligibleTournamentDetail(
                deviceOSType, appBuildType,       // Session related parameters
                playerAttributes, tournamentId);  // Filer/query related parameters
    }


    @POST
    @Path("{tournamentId}/entry")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Enters a Player Tournament",
        notes = "See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#enter-player-tournament")
    public GameOnPlayerTournamentEnterResponse enterTournament(
            @PathParam("tournamentId") final String tournamentId,
            final GameOnPlayerTournamentEnterRequest gameOnPlayerTournamentEnterRequest) {
        return getGameOnPlayerTournamentService().enterTournament(tournamentId, gameOnPlayerTournamentEnterRequest);
    }

    public GameOnPlayerTournamentService getGameOnPlayerTournamentService() {
        return gameOnPlayerTournamentService;
    }

    @Inject
    public void setGameOnPlayerTournamentService(GameOnPlayerTournamentService gameOnPlayerTournamentService) {
        this.gameOnPlayerTournamentService = gameOnPlayerTournamentService;
    }

}
