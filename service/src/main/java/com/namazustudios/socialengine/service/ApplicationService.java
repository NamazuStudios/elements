package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;

import javax.inject.Inject;

/**
 * Manages the lifecycle of the {@link Application} instance.
 *
 * Created by patricktwohig on 7/10/15.
 */
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
     * Deletes an Applicank with the specific name or identifier.
     *
     * @param nameOrId the name, or id
     */
    void deleteApplication(final String nameOrId);

}
