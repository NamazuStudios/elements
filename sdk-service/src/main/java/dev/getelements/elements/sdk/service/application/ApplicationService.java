package dev.getelements.elements.sdk.service.application;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.CreateApplicationRequest;
import dev.getelements.elements.sdk.model.application.UpdateApplicationRequest;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Manages the lifecycle of the {@link Application} instance.
 *
 * Created by patricktwohig on 7/10/15.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface ApplicationService {

    /**
     * Creates a new application and inserts it into the database.  The retruned value
     * represents the {@link CreateApplicationRequest} as it was inserted into the database.
     *
     * @param applicationRequest the application
     *
     * @return the application
     */
    Application createApplication(final CreateApplicationRequest applicationRequest);

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
     * @param applicationRequest the {@link UpdateApplicationRequest} object to write
     * @return the {@link Application} object as it was persisted to the database.
     *
     */
    Application updateApplication(final String nameOrId, final UpdateApplicationRequest applicationRequest);

    /**
     * Deletes an Application with the specific name or identifier.
     *
     * @param nameOrId the name, or id
     */
    void deleteApplication(final String nameOrId);

}
