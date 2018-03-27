package com.namazustudios.socialengine.rest.notifications;

import com.namazustudios.socialengine.model.notification.FCMRegistrationToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

@Api(value = "Firebase Cloud Nofificatios",
     description = "Handles the creation and deletion of the Firebase Cloud Notification registration tokens.  This " +
                   "allows clients to create, read, update, and delete registration info for each of their devices in " +
                   "the system.",
     authorizations = {@Authorization(SESSION_SECRET)})
@Path("notification/fcm")
public class FCMNotificationResource {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public FCMRegistrationToken createRegistrationToken(
            final FCMRegistrationToken fcmRegistrationToken) {
        /// TODO Implement this
        return null;
    }

    @PUT
    @Path("{fcmRegistrationTokenId}")
    @Produces(MediaType.APPLICATION_JSON)
    public FCMRegistrationToken updateRegistrationToken(

            @PathParam("fcmRegistrationTokenId")
            final String fcmRegistrationTokenId,

            final FCMRegistrationToken fcmRegistrationToken) {

        /// TODO Implement this
        return null;
    }

    @DELETE
    @Path("{fcmRegistrationTokenId}")
    @Produces(MediaType.APPLICATION_JSON)
    public FCMRegistrationToken deleteRegistrationToken(

            @PathParam("fcmRegistrationTokenId")
            final String fcmRegistrationTokenId,

            final FCMRegistrationToken fcmRegistrationToken) {

        /// TODO Implement this
        return null;

    }

}
