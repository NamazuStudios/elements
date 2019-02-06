package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.notification.applicationconfiguration.ApplicationConfigurationNotFoundException;
import com.namazustudios.socialengine.exception.notification.NotificationConfigurationException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import static com.namazustudios.socialengine.model.application.ConfigurationCategory.FIREBASE;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;

import java.util.List;

public interface FirebaseApplicationConfigurationDao extends ApplicationConfigurationDao {

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
        return getDefaultApplicationConfigurationForApplication(
                applicationNameOrId,
                FIREBASE,
                FirebaseApplicationConfiguration.class);
    }

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
