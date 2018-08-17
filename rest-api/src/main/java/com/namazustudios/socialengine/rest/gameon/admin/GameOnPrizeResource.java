package com.namazustudios.socialengine.rest.gameon.admin;

import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListRequest;
import com.namazustudios.socialengine.model.gameon.admin.GameOnGetPrizeListResponse;
import com.namazustudios.socialengine.service.GameOnPrizeService;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("game_on_admin/{applicationId}/{configurationId}/prizes")
public class GameOnPrizeResource {

    private GameOnPrizeService gameOnPrizeService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Lists all GameOn Prizes",
            notes = "Lists all prizes using the admin APIs.  Requires Super-User Privileges.  " +
                    "See:  https://developer.amazon.com/docs/gameon/admin-api-ref.html#get-prize-list")
    public GameOnGetPrizeListResponse getPrizes(

        @PathParam("applicationId")
        final String applicationId,

        @PathParam("configurationId")
        final String configurationId

    ) {
        return getGameOnPrizeService().getPrizes(applicationId, configurationId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Create GameOn Prize List",
            notes = "Adds all prizes using the admin APIs.  Requires Super-User Privileges.  " +
                    "See:  https://developer.amazon.com/docs/gameon/admin-api-ref.html#add-prize-list")
    public GameOnAddPrizeListResponse createPrizes(

        @PathParam("applicationId")
        final String applicationId,

        @PathParam("configurationId")
        final String configurationId,

        final GameOnAddPrizeListRequest addPrizeListRequest

    ) {
        return getGameOnPrizeService().addPrizes(applicationId, configurationId, addPrizeListRequest);
    }

    public GameOnPrizeService getGameOnPrizeService() {
        return gameOnPrizeService;
    }

    @Inject
    public void setGameOnPrizeService(GameOnPrizeService gameOnPrizeService) {
        this.gameOnPrizeService = gameOnPrizeService;
    }

}
