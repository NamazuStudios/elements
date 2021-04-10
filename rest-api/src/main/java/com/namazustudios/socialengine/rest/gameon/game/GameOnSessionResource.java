package com.namazustudios.socialengine.rest.gameon.game;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.gameon.game.DeviceOSType;
import com.namazustudios.socialengine.model.gameon.game.GameOnSession;
import com.namazustudios.socialengine.service.GameOnSessionService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

@Api(value = "GameOnSession",
     description = "Handles the creation and deleteion of Amazon GameOn Sessions.  Only one session may exist per " +
                   "Profile and Device OS Type.",
     authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("game_on/session")
public class GameOnSessionResource {

    private ValidationHelper validationHelper;

    private GameOnSessionService gameOnSessionService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Search Game On Sessions",
        notes = "Searches all GameOnSessions in the system and returning the metadata for all matches " +
                "against the given search filter.")
    public Pagination<GameOnSession> getGameOnSessions(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        final String query = nullToEmpty(search).trim();

        return query.isEmpty() ?
                getGameOnSessionService().getGameOnSessions(offset, count) :
                getGameOnSessionService().getGameOnSessions(offset, count, search);

    }

    @GET
    @Path("{gameOnSessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a Specific GameOn Session",
            notes = "Gets a specific profile by profile ID.")
    public GameOnSession getGameOnSession(@PathParam("gameOnSessionId") String gameOnSessionId) {

        gameOnSessionId = Strings.nullToEmpty(gameOnSessionId).trim();

        if (gameOnSessionId.isEmpty()) {
            throw new NotFoundException();
        }

        return getGameOnSessionService().getGameOnSession(gameOnSessionId);

    }

    @GET
    @Path("current")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the current GameOn Session",
            notes = "Gets a specific GameOn Session filtered by the supplied OS type.  This infers the current " +
                    "profile and guarantees that only one session is returned.  This avoisd the client needing to " +
                    "perform needless sifting through the results client side.")
    public GameOnSession getCurrentGameOnSession(
            @QueryParam("os")
            @DefaultValue(DeviceOSType.DEFAULT_TYPE_STRING)
            final DeviceOSType deviceOSType) {
        return getGameOnSessionService().getCurrentGameOnSession(deviceOSType);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a GameOn Session",
            notes = "Supplying a GameOn Session, this will create a new token based on the information supplied " +
                    "to the endpoint.  The response will contain the token as it was written to the database.  Only " +
                    "one GameOnSession may exist per Profile.  However a user may see several " +
                    "GameOnSession instances for their User.  " +
                    "See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#authenticate-player")
    public GameOnSession createSession(final GameOnSession gameOnSession) {

        getValidationHelper().validateModel(gameOnSession, Create.class);

        if (gameOnSession.getId() != null) {
            throw new InvalidDataException("Session token must not specify ID.");
        }

        return getGameOnSessionService().createSession(gameOnSession);

    }

    @DELETE
    @Path("{gameOnSessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteSession(
            @PathParam("gameOnSessionId")
            final String gameOnSessionId) {
        getGameOnSessionService().deleteSession(gameOnSessionId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public GameOnSessionService getGameOnSessionService() {
        return gameOnSessionService;
    }

    @Inject
    public void setGameOnSessionService(GameOnSessionService gameOnSessionService) {
        this.gameOnSessionService = gameOnSessionService;
    }

}
