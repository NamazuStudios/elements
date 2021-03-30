package com.namazustudios.socialengine.rest.gameon.game;


import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.service.GameOnTournamentService;
import io.swagger.annotations.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

@Api(value = "GameOnTournament",
     description = "Provides access to the eligible tournaments.  An eligible tourname is one for which the player " +
                   "qualifies and has not already entered.",
     authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("game_on/tournament/developer")
public class GameOnTournamentResource {

    private GameOnTournamentService gameOnTournamentService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets all available tournaments",
                  notes = "Gets all availble tournaments that the player can enter.  This automatically filters out " +
                          "any tournaments that the player has not alrady entered.  " +
                          "See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#get-tournaments")
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
            final String playerAttributes,

            @QueryParam("eligibleOnly")
            @DefaultValue("true")
            @ApiParam("Set to true to filter tournaments that have not been entered already.")
            final boolean eligibleOnly

    ) {
        return eligibleOnly ?
            getGameOnTournamentService().getEligibleTournaments(
                deviceOSType, appBuildType,             // Session related parameters
                filterBy, limit, period, playerAttributes) :   // Filter/query related parameters
            getGameOnTournamentService().getTournaments(
                deviceOSType, appBuildType,             // Session related parameters
                filterBy, limit, period, playerAttributes);    // Filter/query related parameters
    }

    @GET
    @Path("{tournamentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a single available tournament.",
                  notes = "Gets a single available tournament, specified by the identifier.  " +
                          "See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#get-tournament-details")
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
            final String playerAttributes,

            @QueryParam("eligibleOnly")
            @DefaultValue("true")
            @ApiParam("Set to true to filter tournaments that have not been entered already.")
            final boolean eligibleOnly

    ) {
        return eligibleOnly ?
            getGameOnTournamentService().getEligibleTournamentDetail(
                deviceOSType, appBuildType,       // Session related parameters
                playerAttributes, tournamentId) : // Filer/query related parameters
            getGameOnTournamentService().getTournamentDetail(
                deviceOSType, appBuildType,       // Session related parameters
                playerAttributes, tournamentId);  // Filer/query related parameters
    }

    @POST
    @Path("{tournamentId}/entry")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Enters a Tournament",
            notes = "See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#enter-tournament")
    public GameOnTournamentEnterResponse enterTournament(
            @PathParam("tournamentId") final String tournamentId,
            final GameOnTournamentEnterRequest gameOnTournamentEnterRequest) {
        return getGameOnTournamentService().enterTournament(tournamentId, gameOnTournamentEnterRequest);
    }

    public GameOnTournamentService getGameOnTournamentService() {
        return gameOnTournamentService;
    }

    @Inject
    public void setGameOnTournamentService(GameOnTournamentService gameOnTournamentService) {
        this.gameOnTournamentService = gameOnTournamentService;
    }

}
