package dev.getelements.elements.service;

import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.application.ApplicationConfiguration;
import dev.getelements.elements.model.application.FirebaseApplicationConfiguration;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * The {@Link FirebaseApplicationConfigurationService} manages instances of the {@link FirebaseApplicationConfiguration}
 * within the database.
 */
@Expose({
    @ModuleDefinition(value = "namazu.elements.service.application.configuration.firebase"),
    @ModuleDefinition(
        value = "namazu.elements.service.unscoped.application.configuration.firebase",
        annotation = @ExposedBindingAnnotation(Unscoped.class)
    )
})
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
