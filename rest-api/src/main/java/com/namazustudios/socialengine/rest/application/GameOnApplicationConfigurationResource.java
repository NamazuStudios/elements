package com.namazustudios.socialengine.rest.application;

import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.service.GameOnApplicationConfigurationService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.Objects;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SOCIALENGINE_SESSION_SECRET;

@Api(value = "Firebase Application Configuration",
     description = "Operations for the management of ApplictionConfigurations for Firebase Applications.",
     authorizations = {@Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("application/{applicationNameOrId}/configuration/game_on")
public class GameOnApplicationConfigurationResource {

    private ValidationHelper validationHelper;

    private GameOnApplicationConfigurationService gameOnApplicationConfigurationService;

    /**
     * Gets the specific {@link GameOnApplicationConfiguration} instances assocated with the
     * application.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationConfigurationNameOrId the application profile name or ID
     *
     * @return the {@link GameOnApplicationConfiguration} instance
     */
    @GET
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Gets a GameOn Application Configuration",
            notes = "Gets a single GameOn application based on unique name or ID.")
    public GameOnApplicationConfiguration getGameOnApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        return getGameOnApplicationConfigurationService().getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    /**
     * Creates a new {@link GameOnApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param gameOnApplicationConfiguration the GameOn appliation profile object to creates
     *
     * @return the {@link GameOnApplicationConfiguration} the GameOn Application Configuration
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a new GameOn ApplicationConfiguration",
            notes = "Creates a new GameOn ApplicationConfiguration with the specific ID or application.",
            authorizations = {@Authorization(SESSION_SECRET)})
    public GameOnApplicationConfiguration createGameOnApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final GameOnApplicationConfiguration gameOnApplicationConfiguration) {

        getValidationHelper().validateModel(gameOnApplicationConfiguration);

        if (Objects.equals(gameOnApplicationConfiguration.getParent().getId(), applicationNameOrId) ||
            Objects.equals(gameOnApplicationConfiguration.getParent().getName(), applicationNameOrId)) {
            return getGameOnApplicationConfigurationService().createApplicationConfiguration(applicationNameOrId, gameOnApplicationConfiguration);
        } else {
            throw new InvalidDataException("application name or id mismatch");
        }

    }

    /**
     * Updates an existing {@link GameOnApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param applicationConfigurationNameOrId the name or identifier of the {@link GameOnApplicationConfiguration}
     * @param gameOnApplicationConfiguration the GameOn application profile object to update
     *
     * @return the {@link GameOnApplicationConfiguration} the GameOn Application Configuration
     */
    @PUT
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Updates a GameOn ApplicationConfiguration",
            notes = "Updates an existing GameOn Application profile if it is known to the server.",
            authorizations = {@Authorization(SESSION_SECRET)})
    public GameOnApplicationConfiguration updateApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId,
            final GameOnApplicationConfiguration gameOnApplicationConfiguration) {

        getValidationHelper().validateModel(gameOnApplicationConfiguration);

        if (Objects.equals(gameOnApplicationConfiguration.getParent().getId(), applicationNameOrId) ||
                Objects.equals(gameOnApplicationConfiguration.getParent().getName(), applicationNameOrId)) {
            return getGameOnApplicationConfigurationService().updateApplicationConfiguration(
                    applicationNameOrId,
                    applicationConfigurationNameOrId,
                    gameOnApplicationConfiguration);
        } else {
            throw new InvalidDataException("application name or id mismatch");
        }

    }

    /**
     * Deletes an instance of {@link GameOnApplicationConfiguration}.
     *
     * @param applicationNameOrId the application ID, or name
     * @param applicationConfigurationNameOrId the application profile ID, or name
     */
    @DELETE
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Deletes a GameOn ApplicationConfiguration",
            notes = "Deletes an existing GameOn Application profile if it is known to the server.",
            authorizations = {@Authorization(SESSION_SECRET)})
    public void deleteGameOnApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        getGameOnApplicationConfigurationService().deleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public GameOnApplicationConfigurationService getGameOnApplicationConfigurationService() {
        return gameOnApplicationConfigurationService;
    }

    @Inject
    public void setGameOnApplicationConfigurationService(GameOnApplicationConfigurationService gameOnApplicationConfigurationService) {
        this.gameOnApplicationConfigurationService = gameOnApplicationConfigurationService;
    }


}
