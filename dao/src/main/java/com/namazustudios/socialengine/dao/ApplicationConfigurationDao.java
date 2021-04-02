package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.notification.NotificationConfigurationException;
import com.namazustudios.socialengine.exception.notification.applicationconfiguration.ApplicationConfigurationNotFoundException;
import com.namazustudios.socialengine.model.application.ConfigurationCategory;
import com.namazustudios.socialengine.model.application.ProductBundle;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

import java.util.List;

/**
 * Created by patricktwohig on 7/13/15.
 */
@Expose(@ExposedModuleDefinition("namazu.socialengine.dao.applicationconfiguration"))
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

    /**
     * Gets the first and only {@link ApplicationConfiguration} from for the supplied {@link Application} using
     * the {@link Application#getName()} or {@link Application#getId()} method, then maps and returns an instance of the
     * given class.
     *
     * @param applicationNameOrId the application name or id
     * @return the single {@link ApplicationConfiguration} for the supplied {@link Application}
     */
    default <T extends ApplicationConfiguration> T getDefaultApplicationConfigurationForApplication(
            final String applicationNameOrId,
            final ConfigurationCategory configurationCategory,
            final Class<T> type) {

        final var applicationConfigurationList = getApplicationConfigurationsForApplication(applicationNameOrId, configurationCategory, type);

        if (applicationConfigurationList.isEmpty()) {
            throw new ApplicationConfigurationNotFoundException(
                "No " + configurationCategory.toString() +
                " configuration for application name/id: " + applicationNameOrId);
        } else if (applicationConfigurationList.size() > 1) {
            throw new NotificationConfigurationException(
                applicationConfigurationList.size() + " " + configurationCategory.toString() +
                " configurations for " + applicationNameOrId);
        } else {
            return type.cast(applicationConfigurationList.get(0));
        }

    }

    /**
     * Returns all {@link ApplicationConfiguration} instances for the supplied {@link Application} id and category.
     *
     * @param applicationNameOrId the application name or id
     * @return a {@link List <FirebaseApplicationConfiguration>} associated with the {@link Application}
     */
    <T extends ApplicationConfiguration> List<T> getApplicationConfigurationsForApplication(
            String applicationNameOrId,
            ConfigurationCategory configurationCategory,
            Class<T> type);

    /**
     * Sets the ProductBundle for the given application configuration id.
     *
     * @param applicationConfigurationId the application name or id
     * @param productBundle the product bundle
     * @return
     */
    ApplicationConfiguration updateProductBundles(final String applicationConfigurationId,
                                                  final List<ProductBundle> productBundle);

}
