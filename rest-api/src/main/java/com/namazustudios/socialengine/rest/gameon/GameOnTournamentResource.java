package com.namazustudios.socialengine.rest.gameon;


import com.namazustudios.socialengine.model.gameon.*;
import com.namazustudios.socialengine.service.GameOnTournamentService;
import io.swagger.annotations.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

@Api(value = "Eligible GameOn Tournaments",
     description = "Provides access to the eligible tournaments.  An eligible tourname is one for which the player " +
                   "qualifies and has not already entered.",
     authorizations = {@Authorization(SESSION_SECRET)})
@Path("game_on/tournament/eligible")
public class GameOnTournamentResource {

    private GameOnTournamentService gameOnTournamentService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets all available tournaments",
                  notes = "Gets all availble tournaments that the player can enter.  This automatically filters out " +
                          "any tournaments that the player has not alrady entered.")
    public List<GameOnTournamentSummary> getTournaments(

            @QueryParam("os")
            @DefaultValue(DeviceOSType.DEFAULT_TYPE_STRING)
            final DeviceOSType deviceOSType,

            @QueryParam("build")
            @DefaultValue(AppBuildType.DEFAULT_TYPE_STRING)
            final AppBuildType appBuildType,

            @QueryParam("filterBy")
            final TournamentFilter filterBy,

            @QueryParam("period")
            final TournamentPeriod period,

            @DefaultValue("")
            @QueryParam("playerAttributes")
            @ApiParam("Custom player attributes.")
            final String playerAttributes

    ) {
        return getGameOnTournamentService().getEligibleTournaments(
            deviceOSType, appBuildType,             // Session related parameters
            filterBy, period, playerAttributes);    // Filter/query related parameters
    }

    @GET
    @Path("{tournamentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a single available tournament.",
            notes = "Gets a single available tournament, specified by the identifier.")
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

            @DefaultValue("")
            @QueryParam("playerAttributes")
            @ApiParam("Custom player attributes.")
            final String playerAttributes

    ) {
        return getGameOnTournamentService().getEligibleTournamentDetail(
            deviceOSType, appBuildType,       // Session related parameters
            playerAttributes, tournamentId);  // Filer/query related parameters
    }

    public GameOnTournamentService getGameOnTournamentService() {
        return gameOnTournamentService;
    }

    @Inject
    public void setGameOnTournamentService(GameOnTournamentService gameOnTournamentService) {
        this.gameOnTournamentService = gameOnTournamentService;
    }

}
