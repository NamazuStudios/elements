package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.ProductBundle;

/**
 * Manages the lifecycle of the various {@link ApplicationConfiguration} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
public interface ApplicationConfigurationService {

    /**
     * Gets the applications registered in the databse given the offset and count.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param offset the offset
     * @param count the count
     *
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<ApplicationConfiguration> getApplicationProfiles(final String applicationNameOrId,
                                                                final int offset, final int count);

    /**
     * Gets the applications registered in the databse given the offset and count.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param offset the offset
     * @param count the count
     * @param search a query to filter the results
     *
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<ApplicationConfiguration> getApplicationProfiles(final String applicationNameOrId,
                                                                final int offset, final int count,
                                                                final String search);

    ApplicationConfiguration updateProductBundle(final String applicationConfigurationId,
                                                 final ProductBundle productBundle);

}
