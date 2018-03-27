package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.notification.FCMRegistration;

/**
 * Manages instance of {@link FCMRegistration}.  This provides methods to create, update, and delete registrations
 * with the Firebase Notification Service.
 */
public interface FCMNotificationService {

    /**
     * Creates a new instance of {@link FCMRegistration}.  In addition to ensuring the token has been written to the
     * database, this may perform any other actions necessary such as subscribing the token to a specific topic.
     *
     * @param fcmRegistration the instance of {@link FCMRegistration}
     *
     * @return the {@link FCMRegistration} instance as it was written to the database
     */
    FCMRegistration createRegistrationToken(FCMRegistration fcmRegistration);

    /**
     * Updates an existing instance of {@link FCMRegistration}.  In addition to ensuring the token has been written to
     * the database, this may perform any other actions necessary such as subscribing the token to a specific topic or
     * refreshing an existing registration.
     *
     * @param fcmRegistration the instance of {@link FCMRegistration}
     *
     * @return the {@link FCMRegistration} instance as it was written to the database
     */
    FCMRegistration updateRegistrationToken(FCMRegistration fcmRegistration);

    /**
     * Delets an instance of {@link FCMRegistration} based on the supplied ID.  The ID can be obtained by
     * {@link FCMRegistration#getId()}.  In addition to removing the registration, this may also perform other actions
     * such as unsubscribing from necessary topics.
     *
     * @param fcmRegistrationId as determined by {@link FCMRegistration#getId()}
     */
    void deleteRegistration(String fcmRegistrationId);

}
