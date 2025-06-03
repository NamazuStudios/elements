package dev.getelements.elements.rest.notifications;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.notification.FCMRegistration;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.firebase.FCMRegistrationService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("notification/fcm")
public class FCMRegistrationResource {

    private ValidationHelper validationHelper;

    private FCMRegistrationService fcmRegistrationService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Creates an FCM Registration Token",
        description = "Supplying FCM registration token, this will create a new token based on the information supplied to " +
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update an FCM Registration Token",
            description = "Supplying FCM registration token, this will update the token string with the supplied values.  " +
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
