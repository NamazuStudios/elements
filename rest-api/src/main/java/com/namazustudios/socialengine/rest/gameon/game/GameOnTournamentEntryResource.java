package com.namazustudios.socialengine.rest.gameon.game;

import com.namazustudios.socialengine.model.gameon.game.GameOnTournamentEnterRequest;
import com.namazustudios.socialengine.model.gameon.game.GameOnTournamentEnterResponse;
import com.namazustudios.socialengine.service.GameOnTournamentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

@Api(value = "GameOnEntry",
     description = "Provides access to the eligible tournaments.  An eligible tournament is one for which the player " +
                "qualifies and has not already entered.",
     authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("game_on/tournament")
public class GameOnTournamentEntryResource {

    private GameOnTournamentService gameOnTournamentService;

    @POST
    @Path("{tournamentId}/entry")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a GameOn Registration",
            notes = "Supplying a GameOn Registration, this will create a new token based on the information supplied " +
                    "to the endpoint.  The response will contain the token as it was written to the database.  Only " +
                    "one GameOnRegistration may exist per Profile.  However a user may see several " +
                    "GameOnRegistration instances for their User.  " +
                    "See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#enter-tournament")
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
