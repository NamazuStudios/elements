package dev.getelements.elements.sdk.service.application;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.FirebaseApplicationConfiguration;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * The {@link FirebaseApplicationConfigurationService} manages instances of the {@link FirebaseApplicationConfiguration}
 * within the database.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface FirebaseApplicationConfigurationService {

    /**
     * Gets the {@link FirebaseApplicationConfiguration} for the supplied {@link Application} with the supplied name/id
     * and applicaiton configuration name/id.
     *
     * @param applicationNameOrId the value of {@link Application#getId()} or {@link Application#getName()}
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration#getId()} or {@link ApplicationConfiguration#getName()}
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
     * @param applicationConfigurationNameOrId the {@link ApplicationConfiguration#getId()} or {@link ApplicationConfiguration#getName()}
     */
    void deleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId);

}
