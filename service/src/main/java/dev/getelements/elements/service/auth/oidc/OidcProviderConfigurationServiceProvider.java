package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.OidcProviderConfigurationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.sdk.service.Services.forbidden;

public class OidcProviderConfigurationServiceProvider implements Provider<OidcProviderConfigurationService> {

    private User user;

    private Provider<SuperUserOidcProviderConfigurationService> oidcProviderConfigurationService;

    @Override
    public OidcProviderConfigurationService get() {

        if (SUPERUSER.equals(user.getLevel())) {
            return getOidcProviderConfigurationService().get();
        }

        return forbidden(OidcProviderConfigurationService.class);

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserOidcProviderConfigurationService> getOidcProviderConfigurationService() {
        return oidcProviderConfigurationService;
    }

    @Inject
    public void setOidcProviderConfigurationService(Provider<SuperUserOidcProviderConfigurationService> oidcProviderConfigurationService) {
        this.oidcProviderConfigurationService = oidcProviderConfigurationService;
    }

}
