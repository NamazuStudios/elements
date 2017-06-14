package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;

/**
 * Created by patricktwohig on 7/13/15.
 */
public interface ApplicationConfigurationDao {

    /**
     * Gets the active applications registered in the databse given the offset and count.
     *
     * @param offset the offset
     * @param count the count
     *
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<ApplicationConfiguration> getActiveApplicationConfigurations(final String applicationNameOrId,
                                                                            final int offset, final int count);

    /**
     * Gets the active applications registered in the databse given the offset and count.
     *
     * @param offset the offset
     * @param count the count
     * @param search a query to filter the results
     *
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<ApplicationConfiguration> getActiveApplicationConfigurations(final String applicationNameOrId,
                                                                            final int offset, final int count, final String search);

}
