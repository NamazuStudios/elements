package dev.getelements.elements.rest.notifications;

import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.model.notification.FCMRegistration;
import dev.getelements.elements.service.FCMRegistrationService;
import dev.getelements.elements.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static dev.getelements.elements.rest.swagger.EnhancedApiListingResource.*;

@Api(value = "Firebase Cloud Notifications",
     description = "Handles the creation and deletion of the Firebase Cloud Notification registration tokens.  This " +
                   "allows clients to create, read, update, and delete registration info for each of their devices in " +
                   "the system.",
     authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("notification/fcm")
public class FCMRegistrationResource {

    private ValidationHelper validationHelper;

    private FCMRegistrationService fcmRegistrationService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
        value = "Creates an FCM Registration Token",
        notes = "Supplying FCM registration token, this will create a new token based on the information supplied to " +
                "the endpoint.  The response will contain the token as it was written to the database.  Clients may " +
                "subsequently update the token string with new values as they are issued by Firebase.")
    public FCMRegistration createFCMRegistration(final FCMRegistration fcmRegistration) {

        getValidationHelper().validateModel(fcmRegistration);

        if (fcmRegistration.getId() != null) {
            throw new InvalidDataException("Registration token must not specify ID.");
        }

        return getFcmRegistrationService().createRegistration(fcmRegistration);

    }

    @PUT
    @Path("{fcmRegistrationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Update an FCM Registration Token",
            notes = "Supplying FCM registration token, this will update the token string with the supplied values.  " +
                    "Clients may update the same registration with a different token issued with Firebase if they " +
                    "wish to simply retain the association with the ")
    public FCMRegistration updateRegistration(
            @PathParam("fcmRegistrationId")
            final String fcmRegistrationId,
            final FCMRegistration fcmRegistration) {

        getValidationHelper().validateModel(fcmRegistration);

        if (fcmRegistration.getId() == null) {
            fcmRegistration.setId(fcmRegistrationId);
        } else if (!fcmRegistration.getId().equals(fcmRegistrationId)) {
            throw new InvalidDataException("ID Mismatch in Firebase Registration.  (Value in Object does not match path.)");
        }

        return getFcmRegistrationService().updateRegistration(fcmRegistration);

    }

    @DELETE
    @Path("{fcmRegistrationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteFCMRegistration(
            @PathParam("fcmRegistrationId")
            final String fcmRegistrationId) {
        getFcmRegistrationService().deleteRegistration(fcmRegistrationId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public FCMRegistrationService getFcmRegistrationService() {
        return fcmRegistrationService;
    }

    @Inject
    public void setFcmRegistrationService(FCMRegistrationService fcmRegistrationService) {
        this.fcmRegistrationService = fcmRegistrationService;
    }

}
