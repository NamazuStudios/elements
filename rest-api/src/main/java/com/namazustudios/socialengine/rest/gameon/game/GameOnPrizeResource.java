package com.namazustudios.socialengine.rest.gameon.game;

import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.service.GameOnGamePrizeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SOCIALENGINE_SESSION_SECRET;

@Api(value = "GameOnPlayerTournament",
        description = "Provides access to GameOn player prizes API.",
        authorizations = {@Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{prizeId}")
    @ApiOperation(
            value = "Gets details for the GameOn Prize matching the given id.",
            notes = "Get the prize details which can then be displayed along with the tournament or match details " +
                    "to let players know what they would get if they win. " +
                    "https://developer.amazon.com/docs/gameon/game-api-ref.html#get-prize-details"
    )
    public GameOnGetPrizeDetailsResponse getPrizeDetails(
            @PathParam("prizeId") final String prizeId,
            @QueryParam("deviceOSType") final DeviceOSType deviceOSType,
            @QueryParam("appBuildType") final AppBuildType appBuildType
    ) {
        return getGameOnUserPrizeService().getDetails(prizeId, deviceOSType, appBuildType);
    }


    public GameOnGamePrizeService getGameOnUserPrizeService() {
        return gameOnUserPrizeService;
    }

    @Inject
    public void setGameOnUserPrizeService(GameOnGamePrizeService gameOnUserPrizeService) {
        this.gameOnUserPrizeService = gameOnUserPrizeService;
    }

}
