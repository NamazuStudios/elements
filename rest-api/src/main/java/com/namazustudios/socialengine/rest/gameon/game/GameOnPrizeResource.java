package com.namazustudios.socialengine.rest.gameon.game;

import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.service.GameOnGamePrizeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

@Api(value = "GameOnPlayerTournament",
        description = "Provides access to GameOn player prizes API.",
        authorizations = {@Authorization(SESSION_SECRET)})
@Path("game_on/prize")
public class GameOnPrizeResource {

    private GameOnGamePrizeService gameOnUserPrizeService;

    @POST
    @Path("claim")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Claims Prizes.",
            notes = "See: https://developer.amazon.com/docs/gameon/game-api-ref.html#claim-prizes")
    public GameOnClaimPrizeListResponse claimPrizes(final GameOnClaimPrizeListRequest gameOnClaimPrizeListRequest) {
        return getGameOnUserPrizeService().claim(gameOnClaimPrizeListRequest);
    }

    @POST
    @Path("fulfill")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Fulfills Prizes.",
            notes = "See: https://developer.amazon.com/docs/gameon/game-api-ref.html#fulfill-prizes")
    public GameOnFulfillPrizeListResponse fulfillPrizes(final GameOnFulfillPrizeRequest gameOnFulfillPrizeRequest) {
        return getGameOnUserPrizeService().fulfill(gameOnFulfillPrizeRequest);
    }

    public GameOnGamePrizeService getGameOnUserPrizeService() {
        return gameOnUserPrizeService;
    }

    public void setGameOnUserPrizeService(GameOnGamePrizeService gameOnUserPrizeService) {
        this.gameOnUserPrizeService = gameOnUserPrizeService;
    }

}
