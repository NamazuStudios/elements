package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;

/**
 * The {@Link FirebaseApplicationConfigurationService} manages instances of the {@link FirebaseApplicationConfiguration}
 * within the database.
 */
public interface FirebaseApplicationConfigurationService {

    /**
     * Gets the {@link FirebaseApplicationConfiguration} for the supplied {@link Application} with the supplied name/id
     * and applicaiton configuration name/id.
     *
     * @param applicationNameOrId the value of {@link Application#getId()} or {@link Application#getName()}
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration#getId()} or {@link ApplicationConfiguration#getUniqueIdentifier()}
     * @return the {@link FirebaseApplicationConfiguration} instance
     */
    FirebaseApplicationConfiguration getApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId);

    /**
     * Creates a new instance of {@link FirebaseApplicationConfiguration} and stores in the database, allowing for
     * the specification of the parent {@link Application}.
     *
     * @param applicationNameOrId the value of {@link Application#getId()} or {@link Application#getName()}
     * @param firebaseApplicationConfiguration the {@link FirebaseApplicationConfiguration} to create
     * @return the {@link FirebaseApplicationConfiguration} as it was written to the database
     */
    FirebaseApplicationConfiguration createApplicationConfiguration(String applicationNameOrId, FirebaseApplicationConfiguration firebaseApplicationConfiguration);


    /**
     * Updates an existing instance of {@link FirebaseApplicationConfiguration} and stores in the database, allowing for
     * the specification of the parent {@link Application}.
     *
     * @param applicationNameOrId the value of {@link Application#getId()} or {@link Application#getName()}
     * @param firebaseApplicationConfiguration the {@link FirebaseApplicationConfiguration} to create
     * @return the {@link FirebaseApplicationConfiguration} as it was written to the database
     */
    FirebaseApplicationConfiguration updateApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId, FirebaseApplicationConfiguration firebaseApplicationConfiguration);

    /**
     * Deletes an instance of {@link FirebaseApplicationConfiguration}.
     *
     * @param applicationNameOrId the value of {@link Application#getId()} or {@link Application#getName()}
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration#getId()} or {@link ApplicationConfiguration#getUniqueIdentifier()}
     */
    void deleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId);

}
