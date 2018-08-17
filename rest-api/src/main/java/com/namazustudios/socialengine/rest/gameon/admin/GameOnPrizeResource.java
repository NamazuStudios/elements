package com.namazustudios.socialengine.rest.gameon.admin;

import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListRequest;
import com.namazustudios.socialengine.model.gameon.admin.GameOnGetPrizeListResponse;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("game_on_admin/{applicationId}/{configurationId}/prizes")
public class GameOnPrizeResource {

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
        return null;
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
        return null;
    }


}
