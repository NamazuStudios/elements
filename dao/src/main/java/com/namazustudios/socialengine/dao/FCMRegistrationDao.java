package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.notification.FCMRegistration;
import com.namazustudios.socialengine.model.profile.Profile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

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
     * Gets a {@link Stream<FCMRegistration>} containing all currently registered {@link FCMRegistration} instances
     * associated witht he supplied {@link Profile} id of the recipient.
     *
     * @param recipientId the {@link Profile} id of the recipient, as returned by {@link Profile#getId()}
     * @return a {@link Stream<FCMRegistration>} containing all matching {@link FCMRegistration} instances
     */
    Stream<FCMRegistration> getRegistrationsForRecipient(String recipientId);

    /**
     * Ensures that the {@link Stream} returned will only contain each {@link FCMRegistration} once, and only once.
     * The default implementation of this method uses a technique similar to {@link Stream#distinct()}, but
     * implementations may opt to use the underlying database to optimize this operation.
     *
     * @param recipientId the {@link Profile} id of the recipient, as returned by {@link Profile#getId()}
     * @return a {@link Stream<FCMRegistration>} containing all matching {@link FCMRegistration} instances
     */
    default Stream<FCMRegistration> getDistinctRegistrationsForRecipient(final String recipientId) {
        final Set<Object> dups = ConcurrentHashMap.newKeySet();
        return getRegistrationsForRecipient(recipientId).filter(r -> dups.add(r.getRegistrationToken()));
    }

}
