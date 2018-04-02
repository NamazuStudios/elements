package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.FirebaseApplicationConfigurationNotFoundException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.NotificationConfigurationException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;

import java.util.List;

public interface FirebaseApplicationConfigurationDao {

    /**
     * Creates, or updates an inactive FirebaseApplicationConfiguration object.
     *
     * @param applicationNameOrId
     * @param firebaseApplicationConfiguration
     * @return
     */
    FirebaseApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            String applicationNameOrId,
            FirebaseApplicationConfiguration firebaseApplicationConfiguration);

    /**
     * Gets an {@link FirebaseApplicationConfiguration} with the specific name or identifier.  This
     * may accept either the firebase app ID, or the internal identifier for the configuration.
     *
     * @param applicationConfigurationNameOrId the {@link Application} name or id
     */
    FirebaseApplicationConfiguration getApplicationConfiguration(String applicationConfigurationNameOrId);

    /**
     * Gets the first and only {@link FirebaseApplicationConfiguration} from for the supplied {@link Application} using
     * the {@link Application#getName()} or {@link Application#getId()} method.
     *
     * @param applicationNameOrId the application name or id
     * @return the single {@link FirebaseApplicationConfiguration} for the supplied {@link Application}
     */
    default FirebaseApplicationConfiguration getDefaultFirebaseApplicationConfigurationForApplication(final String applicationNameOrId) {
        final List<FirebaseApplicationConfiguration> firebaseApplicationConfigurationList;
        firebaseApplicationConfigurationList = getFirebaseApplicationConfigurationsForApplication(applicationNameOrId);

        if (firebaseApplicationConfigurationList.isEmpty()) {
            throw new FirebaseApplicationConfigurationNotFoundException("No Firebase configuration for " + applicationNameOrId);
        } else if (firebaseApplicationConfigurationList.size() > 1) {
            throw new NotificationConfigurationException(firebaseApplicationConfigurationList.size() + " Firebase configurations for " + applicationNameOrId);
        } else {
            return firebaseApplicationConfigurationList.get(0);
        }

    }

    /**
     * Returns all {@link FirebaseApplicationConfiguration} instances for the supplied {@link Application} id.
     *
     * @param applicationNameOrId
     * @return a {@link List <FirebaseApplicationConfiguration>} associated with the {@link Application}
     */
    List<FirebaseApplicationConfiguration> getFirebaseApplicationConfigurationsForApplication(String applicationNameOrId);

    /**
     * Gets an {@link FirebaseApplicationConfiguration} with the specific name or identifier
     * combined witht he supplied {@link Application} identifier
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration} id
     */
    FirebaseApplicationConfiguration getApplicationConfiguration(
            String applicationNameOrId,
            String applicationConfigurationNameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationProfileNameOrId the {@link FirebaseApplicationConfiguration} id
     * @param firebaseApplicationConfiguration the {@link FirebaseApplicationConfiguration} object to write
     *
     * @return the {@link FirebaseApplicationConfiguration} object as it was persisted to the database.
     *
     */
    FirebaseApplicationConfiguration updateApplicationConfiguration(
            String applicationNameOrId,
            String applicationProfileNameOrId,
            FirebaseApplicationConfiguration firebaseApplicationConfiguration);

    /**
     * Delets an {@link FirebaseApplicationConfiguration} using the ID as reference.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param applicationConfigurationNameOrId the {@link FirebaseApplicationConfiguration} id
     *
     */
    void softDeleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId);

}
