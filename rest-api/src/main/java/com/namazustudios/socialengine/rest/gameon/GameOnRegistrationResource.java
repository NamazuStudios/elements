package com.namazustudios.socialengine.rest.gameon;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.gameon.GameOnRegistration;
import com.namazustudios.socialengine.service.GameOnRegistrationService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

@Api(value = "GameOn Registration",
    description =
        "Handles the creation and deletion of the GameOn Registrations tokens.  This allows clients to create, read, " +
        "and delete registration.  Only one GameOnRegistration may exist per Profile.",
    authorizations = {@Authorization(SESSION_SECRET)})
@Path("gameon/registration")
public class GameOnRegistrationResource {

    private ValidationHelper validationHelper;

    private GameOnRegistrationService gameOnRegistrationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search Game On Registrations",
            notes = "Searches all GameOnRegistrations in the system and returning the metadata for all matches " +
                    "against the given search filter.")
    public Pagination<GameOnRegistration> getGameOnRegistrations(
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
            getGameOnRegistrationService().getGameOnRegistrations(offset, count) :
            getGameOnRegistrationService().getGameOnRegistrations(offset, count, search);

    }

    @GET
    @Path("{gameOnRegistrationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a Specific Profile",
            notes = "Gets a specific profile by profile ID.")
    public GameOnRegistration getGameOnRegistration(@PathParam("gameOnRegistrationId") String gameOnRegistrationId) {

        gameOnRegistrationId = Strings.nullToEmpty(gameOnRegistrationId).trim();

        if (gameOnRegistrationId.isEmpty()) {
            throw new NotFoundException();
        }

        return getGameOnRegistrationService().getGameOnRegistration(gameOnRegistrationId);

    }

    @GET
    @Path("current")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a Specific Profile",
            notes = "Gets a specific profile by profile ID.")
    public GameOnRegistration getCurrentGameOnRegistration() {
        return getGameOnRegistrationService().getCurrentGameOnRegistration();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a GameOn Registration",
            notes = "Supplying a GameOn Registration, this will create a new token based on the information supplied " +
                    "to the endpoint.  The response will contain the token as it was written to the database.  Only " +
                    "one GameOnRegistration may exist per Profile.  However a user may see several " +
                    "GameOnRegistration instances for their User.")
    public GameOnRegistration createRegistration(final GameOnRegistration gameOnRegistration) {

        getValidationHelper().validateModel(gameOnRegistration, ValidationGroups.Create.class);

        if (gameOnRegistration.getId() != null) {
            throw new InvalidDataException("Registration token must not specify ID.");
        }

        return getGameOnRegistrationService().createRegistration(gameOnRegistration);

    }

    @DELETE
    @Path("{gameOnRegistrationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteRegistration(
            @PathParam("gameOnRegistrationId")
            final String gameOnRegistrationId) {
        getGameOnRegistrationService().deleteRegistration(gameOnRegistrationId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public GameOnRegistrationService getGameOnRegistrationService() {
        return gameOnRegistrationService;
    }

    @Inject
    public void setGameOnRegistrationService(GameOnRegistrationService gameOnRegistrationService) {
        this.gameOnRegistrationService = gameOnRegistrationService;
    }

}
