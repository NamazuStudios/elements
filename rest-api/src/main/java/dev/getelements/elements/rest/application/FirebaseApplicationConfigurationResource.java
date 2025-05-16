package dev.getelements.elements.rest.application;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.application.FirebaseApplicationConfiguration;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.application.FirebaseApplicationConfigurationService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.Objects;

@Path("application/{applicationNameOrId}/configuration/firebase")
public class FirebaseApplicationConfigurationResource {

    private ValidationHelper validationHelper;

    private FirebaseApplicationConfigurationService firebaseApplicationConfigurationService;

    /**
     * Gets the specific {@link FirebaseApplicationConfiguration} instances assocated with the
     * application.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationConfigurationNameOrId the application profile name or ID
     *
     * @return the {@link FirebaseApplicationConfiguration} instance
     */
    @GET
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a Firebase Application Configuration",
            description = "Gets a single Firebase application based on unique name or ID.")
    public FirebaseApplicationConfiguration getFirebaseApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        return getFirebaseApplicationConfigurationService().getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    /**
     * Creates a new {@link FirebaseApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param firebaseApplicationConfiguration the Firebase appliation profile object to creates
     *
     * @return the {@link FirebaseApplicationConfiguration} the Firebase Application Configuration
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new Firebase ApplicationConfiguration",
            description = "Creates a new Firebase ApplicationConfiguration with the specific ID or application.")
    public FirebaseApplicationConfiguration createFirebaseApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {

        getValidationHelper().validateModel(firebaseApplicationConfiguration);

        if (Objects.equals(firebaseApplicationConfiguration.getParent().getId(), applicationNameOrId) ||
                Objects.equals(firebaseApplicationConfiguration.getParent().getName(), applicationNameOrId)) {
            return getFirebaseApplicationConfigurationService().createApplicationConfiguration(applicationNameOrId, firebaseApplicationConfiguration);
        } else {
            throw new InvalidDataException("application name or id mismatch");
        }

    }

    /**
     * Updates an existing {@link FirebaseApplicationConfiguration} isntance.
     *
     * @param applicationNameOrId the applciation name or ID
     * @param applicationConfigurationNameOrId the name or identifier of the {@link FirebaseApplicationConfiguration}
     * @param firebaseApplicationConfiguration the Firebase application profile object to update
     *
     * @return the {@link FirebaseApplicationConfiguration} the Firebase Application Configuration
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates a Firebase ApplicationConfiguration",
            description = "Updates an existing Firebase Application profile if it is known to the server.")
    public FirebaseApplicationConfiguration updateFirebaseApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {

        getValidationHelper().validateModel(firebaseApplicationConfiguration);

        if (Objects.equals(firebaseApplicationConfiguration.getParent().getId(), applicationNameOrId) ||
                Objects.equals(firebaseApplicationConfiguration.getParent().getName(), applicationNameOrId)) {
            return getFirebaseApplicationConfigurationService().updateApplicationConfiguration(
                    applicationNameOrId,
                    applicationConfigurationNameOrId,
                    firebaseApplicationConfiguration);
        } else {
            throw new InvalidDataException("application name or id mismatch");
        }

    }

    /**
     * Deletes an instance of {@link FirebaseApplicationConfiguration}.
     *
     * @param applicationNameOrId the application ID, or name
     * @param applicationConfigurationNameOrId the application profile ID, or name
     */
    @DELETE
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Deletes a Firebase ApplicationConfiguration",
            description = "Deletes an existing Firebase Application profile if it is known to the server.")
    public void deleteFirebaseApplicationConfiguration(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @PathParam("applicationConfigurationNameOrId") final String applicationConfigurationNameOrId) {
        getFirebaseApplicationConfigurationService().deleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public FirebaseApplicationConfigurationService getFirebaseApplicationConfigurationService() {
        return firebaseApplicationConfigurationService;
    }

    @Inject
    public void setFirebaseApplicationConfigurationService(FirebaseApplicationConfigurationService firebaseApplicationConfigurationService) {
        this.firebaseApplicationConfigurationService = firebaseApplicationConfigurationService;
    }

}
