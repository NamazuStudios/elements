package dev.getelements.elements.sdk.service.application;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.List;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Manages the lifecycle of the various {@link ApplicationConfiguration} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
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
     * Gets the applications registered in the database given the offset and count.
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

    ApplicationConfiguration updateProductBundles(final String applicationConfigurationId,
                                                  final List<ProductBundle> productBundles);

}
