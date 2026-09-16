package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.dao.OidcProviderConfigurationDao;
import dev.getelements.elements.sdk.model.auth.OidcAdminLoginProvider;
import dev.getelements.elements.sdk.service.auth.OidcAdminLoginService;
import jakarta.inject.Inject;

import java.util.List;

public class DefaultOidcAdminLoginService implements OidcAdminLoginService {

    private OidcProviderConfigurationDao oidcProviderConfigurationDao;

    @Override
    public List<OidcAdminLoginProvider> getAdminLoginProviders() {
        return getOidcProviderConfigurationDao()
                .getAdminLoginEnabledProviderConfigurations()
                .stream()
                .map(config -> {
                    final var provider = new OidcAdminLoginProvider();
                    provider.setId(config.getId());
                    provider.setName(config.getName());
                    provider.setDisplayName(config.getDisplayName());
                    provider.setIconUrl(config.getIconUrl());
                    return provider;
                })
                .toList();
    }

    public OidcProviderConfigurationDao getOidcProviderConfigurationDao() {
        return oidcProviderConfigurationDao;
    }

    @Inject
    public void setOidcProviderConfigurationDao(OidcProviderConfigurationDao oidcProviderConfigurationDao) {
        this.oidcProviderConfigurationDao = oidcProviderConfigurationDao;
    }

}
