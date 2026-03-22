package dev.getelements.elements.rest.application;

import dev.getelements.elements.sdk.model.application.SteamApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.application.SteamApplicationConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Objects;

@Path("application/{applicationNameOrId}/configuration/steam")
public class SteamApplicationConfigurationResource {

    private ValidationHelper validationHelper;

    private SteamApplicationConfigurationService steamApplicationConfigurationService;

    @GET
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a Steam Application Configuration",
            description = "Gets a single Steam application configuration based on unique name or ID.")
    public SteamApplicationConfiguration getSteamApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        return getSteamApplicationConfigurationService().getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new Steam ApplicationConfiguration",
            description = "Creates a new Steam ApplicationConfiguration for the specified application.")
    public SteamApplicationConfiguration createSteamApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final SteamApplicationConfiguration steamApplicationConfiguration) {

        getValidationHelper().validateModel(steamApplicationConfiguration);

        if (Objects.equals(steamApplicationConfiguration.getParent().getId(), applicationNameOrId) ||
                Objects.equals(steamApplicationConfiguration.getParent().getName(), applicationNameOrId)) {
            return getSteamApplicationConfigurationService().createApplicationConfiguration(applicationNameOrId, steamApplicationConfiguration);
        } else {
            throw new InvalidDataException("application name or id mismatch");
        }
    }

    @PUT
    @Path("{applicationConfigurationNameOrId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates a Steam ApplicationConfiguration",
            description = "Updates an existing Steam application configuration if it is known to the server.")
    public SteamApplicationConfiguration updateSteamApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId,
            final SteamApplicationConfiguration steamApplicationConfiguration) {

        getValidationHelper().validateModel(steamApplicationConfiguration);

        if (Objects.equals(steamApplicationConfiguration.getParent().getId(), applicationNameOrId) ||
                Objects.equals(steamApplicationConfiguration.getParent().getName(), applicationNameOrId)) {
            return getSteamApplicationConfigurationService().updateApplicationConfiguration(
                    applicationNameOrId,
                    applicationConfigurationNameOrId,
                    steamApplicationConfiguration);
        } else {
            throw new InvalidDataException("application name or id mismatch");
        }
    }

    @DELETE
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Deletes a Steam ApplicationConfiguration",
            description = "Deletes an existing Steam application configuration if it is known to the server.")
    public void deleteSteamApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        getSteamApplicationConfigurationService().deleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public SteamApplicationConfigurationService getSteamApplicationConfigurationService() {
        return steamApplicationConfigurationService;
    }

    @Inject
    public void setSteamApplicationConfigurationService(SteamApplicationConfigurationService steamApplicationConfigurationService) {
        this.steamApplicationConfigurationService = steamApplicationConfigurationService;
    }

}
