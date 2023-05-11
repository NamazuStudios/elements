package dev.getelements.elements.service;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Manages the lifecycle of the {@link Application} instance.
 *
 * Created by patricktwohig on 7/10/15.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.application"
        ),
        @ModuleDefinition(
                value = "cci.elements.service.unscoped.application",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.application",
                deprecated = @DeprecationDefinition("Use eci.elements.service.application instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.application",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use cci.elements.service.unscoped.application instead.")
        )
})
public interface ApplicationService {

    /**
     * Creates a new application and inserts it into the database.  The retruned value
     * represents the {@link Application} as it was inserted into the database.
     *
     * @param application the application
     *
     * @return the application
     */
    Application createApplication(final Application application);

    /**
     * Lists all {@link Application} instances avaiable to the current user.
     *
     * @return a {@link Pagination<Application>} for all available instances.
     */
    Pagination<Application> getApplications();

    /**
     * Gets the applications registered in the databse given the offset and count.
     *
     * @param offset the offset
     * @param count the count
     *
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<Application> getApplications(final int offset, final int count);

    /**
     * Gets the applications registered in the databse given the offset and count.
     *
     * @param offset the offset
     * @param count the count
     * @param search a query to filter the results
     *
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<Application> getApplications(final int offset, final int count, final String search);

    /**
     * Gets an application with the specific name or identifier.
     */
    Application getApplication(final String nameOrId);

    /**
     * Updates an application with the specific name/identifiers.
     *
     * @param nameOrId the name, or id
     * @param application the {@link Application} object to write
     * @return the {@link Application} object as it was persisted to the database.
     *
     */
    Application updateApplication(final String nameOrId, final Application application);

    /**
     * Deletes an Application with the specific name or identifier.
     *
     * @param nameOrId the name, or id
     */
    void deleteApplication(final String nameOrId);

}
