package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.dao.OidcProviderConfigurationDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOidcProviderConfigurationRequest;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOidcProviderConfigurationResponse;
import dev.getelements.elements.sdk.model.auth.OidcProviderConfiguration;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.OidcProviderConfigurationService;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

import static dev.getelements.elements.sdk.model.Constants.API_OUTSIDE_URL;

public class SuperUserOidcProviderConfigurationService implements OidcProviderConfigurationService {

    private OidcProviderConfigurationDao oidcProviderConfigurationDao;

    private OidcProviderConfigurationOperations oidcProviderConfigurationOperations;

    private ValidationHelper validationHelper;

    private String apiOutsideUrl;

    @Override
    public Pagination<OidcProviderConfiguration> getProviderConfigurations(final int offset,
                                                                            final int count,
                                                                            final List<String> tags) {
        return getOidcProviderConfigurationDao()
                .getProviderConfigurations(offset, count, tags)
                .transform(this::redact);
    }

    @Override
    public OidcProviderConfiguration getProviderConfiguration(final String providerConfigurationId) {
        return redact(getOidcProviderConfigurationDao().getProviderConfiguration(providerConfigurationId));
    }

    private OidcProviderConfiguration redact(final OidcProviderConfiguration config) {
        // The client secret authenticates this server to the provider; it must never be readable back through
        // the API once set, regardless of caller privilege.
        config.setClientSecret(null);
        return config;
    }

    @Override
    public CreateOrUpdateOidcProviderConfigurationResponse createProviderConfiguration(
            final CreateOrUpdateOidcProviderConfigurationRequest request) {

        getValidationHelper().validateModel(request, ValidationGroups.Create.class);

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

        getValidationHelper().validateModel(request, ValidationGroups.Update.class);

        final var config = new OidcProviderConfiguration();
        config.setId(providerConfigurationId);
        applyRequest(config, request);

        if (request.getClientSecret() == null || request.getClientSecret().isBlank()) {
            // Blank means "leave unchanged" on update; the client secret is never readable back through the
            // API, so the caller has no way to resupply the existing value.
            final var existing = getOidcProviderConfigurationDao().getProviderConfiguration(providerConfigurationId);
            config.setClientSecret(existing.getClientSecret());
        }

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
        config.setRedirectUri(resolveRedirectUri(request.getProvider(), request.getRedirectUri()));
        config.setExtraAuthorizeParams(request.getExtraAuthorizeParams());
        config.setTokenEndpointAuthMethod(request.getTokenEndpointAuthMethod());
    }

    private String resolveRedirectUri(final String provider, final String requestedRedirectUri) {

        if (requestedRedirectUri != null && !requestedRedirectUri.isBlank()) {
            return requestedRedirectUri;
        }

        final var base = getApiOutsideUrl().endsWith("/")
                ? getApiOutsideUrl().substring(0, getApiOutsideUrl().length() - 1)
                : getApiOutsideUrl();

        return base + "/oidc/" + provider + "/callback";

    }

    private CreateOrUpdateOidcProviderConfigurationResponse toResponse(final OidcProviderConfiguration config,
                                                                        final OidcDiscoveryDocument discoveryDocument) {

        final var response = new CreateOrUpdateOidcProviderConfigurationResponse();
        response.setConfiguration(redact(config));
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

    public String getApiOutsideUrl() {
        return apiOutsideUrl;
    }

    @Inject
    public void setApiOutsideUrl(@Named(API_OUTSIDE_URL) String apiOutsideUrl) {
        this.apiOutsideUrl = apiOutsideUrl;
    }

}
