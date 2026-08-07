package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.auth.OidcProviderConfiguration;
import dev.getelements.elements.sdk.model.exception.auth.OidcProviderConfigurationNotFoundException;

import java.util.List;
import java.util.Optional;

/** DAO for {@link OidcProviderConfiguration} instances, admin-managed via SUPERUSER CRUD. */
@ElementServiceExport
public interface OidcProviderConfigurationDao {

    /**
     * Lists all provider configurations.
     *
     * @param offset the offset
     * @param count the count
     * @param tags the tags to filter by
     * @return a {@link Pagination} of {@link OidcProviderConfiguration} instances
     */
    Pagination<OidcProviderConfiguration> getProviderConfigurations(int offset, int count, List<String> tags);

    /**
     * Finds a provider configuration by its unique id.
     *
     * @param providerConfigurationId the id
     * @return an {@link Optional} containing the configuration, or empty if absent
     */
    Optional<OidcProviderConfiguration> findProviderConfiguration(String providerConfigurationId);

    /**
     * Fetches a provider configuration by its unique id, throwing if absent.
     *
     * @param providerConfigurationId the id
     * @return the configuration, never null
     */
    default OidcProviderConfiguration getProviderConfiguration(final String providerConfigurationId) {
        return findProviderConfiguration(providerConfigurationId)
                .orElseThrow(OidcProviderConfigurationNotFoundException::new);
    }

    /**
     * Finds a provider configuration by its unique name (e.g. "twitch").
     *
     * @param name the provider's unique name
     * @return an {@link Optional} containing the configuration, or empty if absent
     */
    Optional<OidcProviderConfiguration> findByName(String name);

    /**
     * Creates a new provider configuration.
     *
     * @param providerConfiguration the configuration to create
     * @return the created configuration
     */
    OidcProviderConfiguration createProviderConfiguration(OidcProviderConfiguration providerConfiguration);

    /**
     * Updates an existing provider configuration.
     *
     * @param providerConfiguration the configuration to update
     * @return the updated configuration
     */
    OidcProviderConfiguration updateProviderConfiguration(OidcProviderConfiguration providerConfiguration);

    /**
     * Deletes a provider configuration.
     *
     * @param providerConfigurationId the id
     */
    void deleteProviderConfiguration(String providerConfigurationId);

}
