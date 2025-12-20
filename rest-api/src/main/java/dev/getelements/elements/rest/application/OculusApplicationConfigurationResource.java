package dev.getelements.elements.rest.application;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.application.OculusApplicationConfiguration;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.application.OculusApplicationConfigurationService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.Objects;

/**
 * Created by patricktwohig on 6/14/17.
 */
@Path("application/{applicationNameOrId}/configuration/oculus")
public class OculusApplicationConfigurationResource {

    private ValidationHelper validationHelper;

    private OculusApplicationConfigurationService oculusApplicationConfigurationService;

    /**
     * Gets the specific {@link OculusApplicationConfiguration} instances associated with the
     * application.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationConfigurationNameOrId the application profile name or ID
     *
     * @return the {@link OculusApplicationConfiguration} instance
     */
    @GET
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a Oculus Application Configuration",
            description = "Gets a single Oculus application based on unique name or ID.")
    public OculusApplicationConfiguration getOculusApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        return getOculusApplicationConfigurationService().getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    /**
     * Creates a new {@link OculusApplicationConfiguration} instance.
     *
     * @param applicationNameOrId the application name or ID
     * @param oculusApplicationConfiguration the Oculus application profile object to creates
     *
     * @return the {@link OculusApplicationConfiguration} the Oculus Application Configuration
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new Oculus ApplicationConfiguration",
            description = "Creates a new Oculus ApplicationConfiguration with the specific ID or application.")
    public OculusApplicationConfiguration createOculusApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final OculusApplicationConfiguration oculusApplicationConfiguration) {

        getValidationHelper().validateModel(oculusApplicationConfiguration);

        if (Objects.equals(oculusApplicationConfiguration.getParent().getId(), applicationNameOrId) ||
                Objects.equals(oculusApplicationConfiguration.getParent().getName(), applicationNameOrId)) {
            return getOculusApplicationConfigurationService().createApplicationConfiguration(applicationNameOrId, oculusApplicationConfiguration);
        } else {
            throw new InvalidDataException("application name or id mismatch");
        }

    }

    /**
     * Updates an existing {@link OculusApplicationConfiguration} instance.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationConfigurationNameOrId the name or identifier of the {@link OculusApplicationConfiguration}
     * @param oculusApplicationConfiguration the Oculus application profile object to update
     *
     * @return the {@link OculusApplicationConfiguration} the Oculus Application Configuration
     */
    @PUT
    @Path("{applicationConfigurationNameOrId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates a Oculus ApplicationConfiguration",
            description = "Updates an existing Oculus Application profile if it is known to the server.")
    public OculusApplicationConfiguration updateOculusApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId,
            final OculusApplicationConfiguration oculusApplicationConfiguration) {

        getValidationHelper().validateModel(oculusApplicationConfiguration);

        if (Objects.equals(oculusApplicationConfiguration.getParent().getId(), applicationNameOrId) ||
                Objects.equals(oculusApplicationConfiguration.getParent().getName(), applicationNameOrId)) {
            return getOculusApplicationConfigurationService().updateApplicationConfiguration(
                    applicationNameOrId,
                    applicationConfigurationNameOrId,
                    oculusApplicationConfiguration);
        } else {
            throw new InvalidDataException("application name or id mismatch");
        }

    }

    /**
     * Deletes an instance of {@link OculusApplicationConfiguration}.
     *
     * @param applicationNameOrId the application ID, or name
     * @param applicationConfigurationNameOrId the application profile ID, or name
     */
    @DELETE
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Deletes a Oculus ApplicationConfiguration",
            description = "Deletes an existing Oculus Application profile if it is known to the server.")
    public void deleteOculusApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        getOculusApplicationConfigurationService().deleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public OculusApplicationConfigurationService getOculusApplicationConfigurationService() {
        return oculusApplicationConfigurationService;
    }

    @Inject
    public void setOculusApplicationConfigurationService(OculusApplicationConfigurationService oculusApplicationConfigurationService) {
        this.oculusApplicationConfigurationService = oculusApplicationConfigurationService;
    }

}
