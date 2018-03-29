package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;
import com.namazustudios.socialengine.model.notification.FCMRegistration;
import com.namazustudios.socialengine.model.profile.Profile;

import java.util.List;

/**
 * Manipulates instances of {@link FCMRegistration} in the underlying database.
 */
public interface FCMRegistrationDao {

    /**
     * Creates an instance of {@link FCMRegistration} and stores it in the database.
     *
     * @param fcmRegistration the {@link FCMRegistration}
     *
     * @return the {@link FCMRegistration} instance as it was created in the database
     */
    FCMRegistration createRegistration(FCMRegistration fcmRegistration);

    /**
     * Updates an instance of {@link FCMRegistration} in the database.  Reassigning the values of
     * {@link FCMRegistration#getProfile()} or {@link FCMRegistration#getRegistrationToken()} as necessary
     *
     * @param fcmRegistration the {@link FCMRegistration} instance
     * @return the {@link FCMRegistration} isntance as it was written to the database
     */
    FCMRegistration updateRegistration(FCMRegistration fcmRegistration);

    /**
     * Deletes an instance of {@link FCMRegistration} based on the supplied id.  The id value corresponds to the
     * value returned by {@link FCMRegistration#getId()}.
     *
     * @param fcmRegistrationId the id of the {@link FCMRegistration} as supplied by {@link FCMRegistration#getId()}
     */
    void deleteRegistration(String fcmRegistrationId);

    /**
     * Deletes an instance of {@link FCMRegistration} based on the supplied id.  The id value corresponds to the
     * value returned by {@link FCMRegistration#getId()}.  Additionally, this must refuse the request if the supplied
     * {@link Profile} does not match the owner of the correspendong {@link FCMRegistration}.
     *
     * @param fcmRegistrationId the id of the {@link FCMRegistration} as supplied by {@link FCMRegistration#getId()}
     */
    void deleteRegistrationWithRequestingProfile(Profile profile, String fcmRegistrationId);

    /**
     * Gets a {@link List<FCMRegistration>} containing all currently registered {@link FCMRegistration} instances
     * associated witht he supplied {@link Profile} id of the recipient.
     *
     * @param applicationId the {@link Application} id, as returned by {@link Application#getId()}
     * @param recipientId the {@link Profile} id of the recipient, as returned by {@link Profile#getId()}
     * @return
     */
    List<FCMRegistration> getRegistrationsForRecipient(String applicationId, String recipientId);

}
