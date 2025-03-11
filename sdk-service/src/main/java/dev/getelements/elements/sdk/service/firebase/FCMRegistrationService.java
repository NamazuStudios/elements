package dev.getelements.elements.sdk.service.firebase;

import dev.getelements.elements.sdk.model.notification.FCMRegistration;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Manages instance of {@link FCMRegistration}.  This provides methods to create, update, and delete registrations
 * with the Firebase Notification Service.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface FCMRegistrationService {

    /**
     * Creates a new instance of {@link FCMRegistration}.  In addition to ensuring the token has been written to the
     * database, this may perform any other actions necessary such as subscribing the token to a specific topic.
     *
     * @param fcmRegistration the instance of {@link FCMRegistration}
     *
     * @return the {@link FCMRegistration} instance as it was written to the database
     */
    FCMRegistration createRegistration(FCMRegistration fcmRegistration);

    /**
     * Updates an existing instance of {@link FCMRegistration}.  In addition to ensuring the token has been written to
     * the database, this may perform any other actions necessary such as subscribing the token to a specific topic or
     * refreshing an existing registration.
     *
     * @param fcmRegistration the instance of {@link FCMRegistration}
     *
     * @return the {@link FCMRegistration} instance as it was written to the database
     */
    FCMRegistration updateRegistration(FCMRegistration fcmRegistration);

    /**
     * Delets an instance of {@link FCMRegistration} based on the supplied ID.  The ID can be obtained by
     * {@link FCMRegistration#getId()}.  In addition to removing the registration, this may also perform other actions
     * such as unsubscribing from necessary topics.
     *
     * @param fcmRegistrationId as determined by {@link FCMRegistration#getId()}
     */
    void deleteRegistration(String fcmRegistrationId);

}
