package com.namazustudios.socialengine.rest.gameon;


import com.namazustudios.socialengine.model.gameon.DeviceOSType;
import com.namazustudios.socialengine.model.gameon.GameOnTournamentDetail;
import com.namazustudios.socialengine.model.gameon.GameOnTournamentSummary;
import io.swagger.annotations.*;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets all available tournaments",
                  notes = "Gets all availble tournaments that the player can enter.  This automatically filters out " +
                          "any tournaments that the player has not alrady entered.")
    public List<GameOnTournamentSummary> getTournaments(

            @QueryParam("os")
            @DefaultValue(DeviceOSType.DEFAULT_TYPE_STRING)
            final DeviceOSType deviceOSType,

            @QueryParam("filterBy")
            final Filter filterBy,

            @DefaultValue("")
            @QueryParam("playerAttributes")
            @ApiParam("Custom player attributes.")
            final String playerAttributes

    ) {
        // TODO Return All Tournaments
        return null;
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

            @QueryParam("filterBy")
            final Filter filterBy,

            @DefaultValue("")
            @QueryParam("playerAttributes")
            @ApiParam("Custom player attributes.")
            final String playerAttributes

    ) {
        // TODO Return All Tournaments
        return null;
    }

    @ApiModel("Tournament filter enumeration.")
    public enum Filter {
        live,
        upcoming
    }


}
