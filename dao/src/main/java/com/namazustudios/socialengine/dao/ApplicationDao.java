package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.rt.annotation.DeprecationDefinition;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Created by patricktwohig on 7/10/15.
 */
@Expose({
    @ExposedModuleDefinition("namazu.elements.dao.application"),
    @ExposedModuleDefinition(
        value = "namazu.socialengine.dao.application",
        deprecated = @DeprecationDefinition("Use namazu.elements.dao.application instead"))
})
public interface ApplicationDao {

    /**
     * Creates a new application and inserts it into the database.  The returned value
     * represents the {@link Application} as it was inserted into the database.
     *
     * If the existing {@link Application} objects is int eh database however is flagged
     * as hidden or inactive this will simply reinstate the old record.
     *
     * @param application the application
     *
     * @return the application instance as it was created
     */
    Application createOrUpdateInactiveApplication(final Application application);

    /**
     * Gets all active applications.
     *
     * @return a {@link Pagination<Application>} of all active {@link Application} instances
     */
    Pagination<Application> getActiveApplications();

    /**
     * Gets the active applications registered in the databse given the offset and count.
     *
     * @param offset the offset
     * @param count the count
     *
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<Application> getActiveApplications(final int offset, final int count);

    /**
     * Gets the active applications registered in the databse given the offset and count.
     *
     * @param offset the offset
     * @param count the count
     * @param search a query to filter the results
     *
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<Application> getActiveApplications(final int offset, final int count, final String search);

    /**
     * Gets an application with the specific name or identifier.  This will throw an instance
     * of {@link NotFoundException} if the object cannot be found, or is inactive.
     *
     * @return an Application instance, never null
     * @throws NotFoundException if the application is inactive or non-existent
     */
    Application getActiveApplication(final String nameOrId);

    /**
     * Updates an application with the specific name/identifiers.  When changing
     * the application name the ID must be specified.
     *
     * @param nameOrId the name, or id
     * @param application the {@link Application} object to write
     *
     * @throws NotFoundException if the application does not exist.
     *
     * @return the {@link Application} object as it was persisted to the database.
     *
     */
    Application updateActiveApplication(final String nameOrId, final Application application);

    /**
     * Deletes an Application with the specific name or identifier.  The application is no
     * actually removed, but rather it is flagged as inactive to preserve any
     * consistency with dependent data.
     *
     * @param nameOrId the name, or id
     */
    void softDeleteApplication(final String nameOrId);

}
