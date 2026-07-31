package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.dao.OidcProviderConfigurationDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOidcProviderConfigurationRequest;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOidcProviderConfigurationResponse;
import dev.getelements.elements.sdk.model.auth.OidcProviderConfiguration;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.OidcProviderConfigurationService;
import jakarta.inject.Inject;

import java.util.List;

public class SuperUserOidcProviderConfigurationService implements OidcProviderConfigurationService {

    private OidcProviderConfigurationDao oidcProviderConfigurationDao;

    private OidcProviderConfigurationOperations oidcProviderConfigurationOperations;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<OidcProviderConfiguration> getProviderConfigurations(final int offset,
                                                                            final int count,
                                                                            final List<String> tags) {
        return getOidcProviderConfigurationDao().getProviderConfigurations(offset, count, tags);
    }

    @Override
    public OidcProviderConfiguration getProviderConfiguration(final String providerConfigurationId) {
        return getOidcProviderConfigurationDao().getProviderConfiguration(providerConfigurationId);
    }

    @Override
    public CreateOrUpdateOidcProviderConfigurationResponse createProviderConfiguration(
            final CreateOrUpdateOidcProviderConfigurationRequest request) {

        getValidationHelper().validateModel(request);

        final var config = new OidcProviderConfiguration();
        applyRequest(config, request);

        // Fail fast on a bad discoveryUrl, and auto-provision the matching OidcAuthScheme by issuer, before
        // persisting the configuration.
        final var discoveryDocument = getOidcProviderConfigurationOperations().resolveDiscovery(config);
        getOidcProviderConfigurationOperations().resolveScheme(config, discoveryDocument);

        final var created = getOidcProviderConfigurationDao().createProviderConfiguration(config);

        return toResponse(created, discoveryDocument);

    }

    @Override
    public CreateOrUpdateOidcProviderConfigurationResponse updateProviderConfiguration(
            final String providerConfigurationId,
            final CreateOrUpdateOidcProviderConfigurationRequest request) {

        getValidationHelper().validateModel(request);

        final var config = new OidcProviderConfiguration();
        config.setId(providerConfigurationId);
        applyRequest(config, request);

        final var discoveryDocument = getOidcProviderConfigurationOperations().resolveDiscovery(config);
        getOidcProviderConfigurationOperations().resolveScheme(config, discoveryDocument);

        final var updated = getOidcProviderConfigurationDao().updateProviderConfiguration(config);

        return toResponse(updated, discoveryDocument);

    }

    @Override
    public void deleteProviderConfiguration(final String providerConfigurationId) {
        getOidcProviderConfigurationDao().deleteProviderConfiguration(providerConfigurationId);
    }

    private void applyRequest(final OidcProviderConfiguration config,
                               final CreateOrUpdateOidcProviderConfigurationRequest request) {
        config.setProvider(request.getProvider());
        config.setDiscoveryUrl(request.getDiscoveryUrl());
        config.setClientId(request.getClientId());
        config.setClientSecret(request.getClientSecret());
        config.setScopes(request.getScopes());
        config.setRedirectUri(request.getRedirectUri());
        config.setExtraAuthorizeParams(request.getExtraAuthorizeParams());
        config.setTokenEndpointAuthMethod(request.getTokenEndpointAuthMethod());
    }

    private CreateOrUpdateOidcProviderConfigurationResponse toResponse(final OidcProviderConfiguration config,
                                                                        final OidcDiscoveryDocument discoveryDocument) {

        // Never echo the client secret back to the caller.
        config.setClientSecret(null);

        final var response = new CreateOrUpdateOidcProviderConfigurationResponse();
        response.setConfiguration(config);
        response.setIssuer(discoveryDocument.getIssuer());
        response.setAuthorizationEndpoint(discoveryDocument.getAuthorizationEndpoint());
        response.setTokenEndpoint(discoveryDocument.getTokenEndpoint());

        return response;

    }

    public OidcProviderConfigurationDao getOidcProviderConfigurationDao() {
        return oidcProviderConfigurationDao;
    }

    @Inject
    public void setOidcProviderConfigurationDao(OidcProviderConfigurationDao oidcProviderConfigurationDao) {
        this.oidcProviderConfigurationDao = oidcProviderConfigurationDao;
    }

    public OidcProviderConfigurationOperations getOidcProviderConfigurationOperations() {
        return oidcProviderConfigurationOperations;
    }

    @Inject
    public void setOidcProviderConfigurationOperations(OidcProviderConfigurationOperations oidcProviderConfigurationOperations) {
        this.oidcProviderConfigurationOperations = oidcProviderConfigurationOperations;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
