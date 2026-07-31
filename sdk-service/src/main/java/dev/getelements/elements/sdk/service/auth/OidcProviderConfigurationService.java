package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOidcProviderConfigurationRequest;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOidcProviderConfigurationResponse;
import dev.getelements.elements.sdk.model.auth.OidcProviderConfiguration;

import java.util.List;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/** SUPERUSER-scoped CRUD service for {@link OidcProviderConfiguration} instances. */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface OidcProviderConfigurationService {

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
     * Fetches a specific provider configuration by ID. If not found, an exception is raised.
     *
     * @param providerConfigurationId the provider configuration ID
     * @return the {@link OidcProviderConfiguration}, never null
     */
    OidcProviderConfiguration getProviderConfiguration(String providerConfigurationId);

    /**
     * Creates a new provider configuration, eagerly resolving its discovery document to fail fast on a bad
     * {@code discoveryUrl} and auto-provisioning the matching {@link dev.getelements.elements.sdk.model.auth.OidcAuthScheme}
     * by issuer if one does not already exist.
     *
     * @param request the request with the information to create the provider configuration
     * @return the response, with the client secret cleared
     */
    CreateOrUpdateOidcProviderConfigurationResponse createProviderConfiguration(CreateOrUpdateOidcProviderConfigurationRequest request);

    /**
     * Updates an existing provider configuration.
     *
     * @param providerConfigurationId the provider configuration ID
     * @param request the request with the information to update the provider configuration
     * @return the response, with the client secret cleared
     */
    CreateOrUpdateOidcProviderConfigurationResponse updateProviderConfiguration(String providerConfigurationId, CreateOrUpdateOidcProviderConfigurationRequest request);

    /**
     * Deletes the provider configuration with the supplied ID.
     *
     * @param providerConfigurationId the provider configuration ID
     */
    void deleteProviderConfiguration(String providerConfigurationId);

}
