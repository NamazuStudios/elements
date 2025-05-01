package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ConfigurationCategory;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.model.exception.application.ApplicationConfigurationNotFoundException;
import dev.getelements.elements.sdk.model.exception.notification.NotificationConfigurationException;

import java.util.List;
import java.util.Optional;

/**
 * Created by patricktwohig on 7/13/15.
 */
@ElementServiceExport
public interface ApplicationConfigurationDao {

    /**
     * Gets the active applications registered in the databse given the offset and count.
     *
     * @param offset the offset
     * @param count  the count
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<ApplicationConfiguration> getActiveApplicationConfigurations(String applicationNameOrId,
                                                                            int offset, int count);

    /**
     * Gets the active applications registered in the databse given the offset and count.
     *
     * @param offset the offset
     * @param count  the count
     * @param search a query to filter the results
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<ApplicationConfiguration> getActiveApplicationConfigurations(
            String applicationNameOrId,
            int offset, int count, String search);

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
            final ConfigurationCategory configurationCategory) {

        final var applicationConfigurationList = getApplicationConfigurationsForApplication(
                applicationNameOrId,
                configurationCategory
        );

        if (applicationConfigurationList.isEmpty()) {
            throw new ApplicationConfigurationNotFoundException(
                    "No " + configurationCategory.toString() +
                            " configuration for application name/id: " + applicationNameOrId);
        } else if (applicationConfigurationList.size() > 1) {
            throw new NotificationConfigurationException(
                    applicationConfigurationList.size() + " " + configurationCategory.toString() +
                            " configurations for " + applicationNameOrId);
        } else {
            return (T) applicationConfigurationList.getFirst();
        }

    }

    /**
     * Returns all {@link ApplicationConfiguration} instances for the supplied {@link Application} id and category.
     *
     * @param applicationNameOrId the application name or id
     * @return a {@link List} associated with the {@link Application}
     */
    <T extends ApplicationConfiguration> List<T> getApplicationConfigurationsForApplication(
            String applicationNameOrId,
            ConfigurationCategory configurationCategory);

    /**
     * Sets the ProductBundle for the given application configuration id.
     *
     * @param applicationConfigurationId the application name or id
     * @param productBundle              the product bundle
     * @return
     */
    ApplicationConfiguration updateProductBundles(
            String applicationConfigurationId,
            List<ProductBundle> productBundle
    );

    /**
     * Creates the application configuration.
     *
     * @param applicationNameOrId
     * @return the {@link ApplicationConfiguration} as written to the database
     * @param <T> the type of application configuration
     */
    <T extends ApplicationConfiguration>
    T createApplicationConfiguration(
            String applicationNameOrId,
            T applicationConfiguration
    );

    /**
     * Updates the application configuration.
     *
     * @param applicationNameOrId
     * @return the {@link ApplicationConfiguration} as written to the database
     * @param <T> the type of application configuration
     */
    <T extends ApplicationConfiguration>
    T updateApplicationConfiguration(
            String applicationNameOrId,
            String applicationConfigurationId,
            T applicationConfiguration
    );

    /**
     * Gets the {@link ApplicationConfiguration} with the supplied name and id.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationConfigurationId the application configuration ID
     * @return the instance
     * @param <T>
     */
    <T extends ApplicationConfiguration>
    Optional<T> findApplicationConfiguration(
            Class<T> configT,
            String applicationNameOrId,
            String applicationConfigurationId
    );

    /**
     * Deletes the application configuration.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationConfigurationId the application configuration ID
     */
    void deleteApplicationConfiguration(
            String applicationNameOrId,
            String applicationConfigurationId
    );

    /**
     * Gets the {@link ApplicationConfiguration} with the supplied name and id.
     *
     * @param applicationNameOrId the application name or ID
     * @param applicationConfigurationId the application configuration ID
     * @return the instance
     * @param <T>
     */
    default <T extends ApplicationConfiguration>
    T getApplicationConfiguration(
            final Class<T> configT,
            final String applicationNameOrId,
            final String applicationConfigurationId
    ) {
        return findApplicationConfiguration(
                configT,
                applicationNameOrId,
                applicationConfigurationId
        ).orElseThrow(ApplicationConfigurationNotFoundException::new);
    }

}
