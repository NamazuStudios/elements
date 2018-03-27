package com.namazustudios.socialengine.rest.application;

import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;
import com.namazustudios.socialengine.service.FirebaseApplicationConfigurationService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

@Api(
    value = "Firebase Application Configuration",
    description = "Operations for the management of ApplictionConfigurations for Firebase Applications.",
    authorizations = {@Authorization(SESSION_SECRET)})
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
    @ApiOperation(
            value = "Gets a Firebase Application Configuration",
            notes = "Gets a single Firebase application based on unique name or ID.")
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
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a new Firebase ApplicationConfiguration",
            notes = "Creates a new Firebase ApplicationConfiguration with the specific ID or application.")
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
    @Path("{applicationConfigurationNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Updates a Firebase ApplicationConfiguration",
            notes = "Updates an existing Firebase Application profile if it is known to the server.")
    public FirebaseApplicationConfiguration updateApplicationConfiguration(
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
    @ApiOperation(
            value = "Deletes a Firebase ApplicationConfiguration",
            notes = "Deletes an existing Firebase Application profile if it is known to the server.")
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
