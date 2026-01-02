package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.auth.OidcAuthService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class OidcAuthServiceProvider implements Provider<OidcAuthService> {

    private User user;

    private Provider<AnonOidcAuthService> anonOidcAuthServiceProvider;

    private Provider<UserOidcAuthService> getUserOidcAuthServiceProvider;

    @Override
    public OidcAuthService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:
                return getUserOidcAuthServiceProvider().get();
            default:
                return getAnonOidcAuthServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonOidcAuthService> getAnonOidcAuthServiceProvider() {
        return anonOidcAuthServiceProvider;
    }

    @Inject
    public void setAnonOidcAuthServiceProvider(Provider<AnonOidcAuthService> anonOidcAuthServiceProvider) {
        this.anonOidcAuthServiceProvider = anonOidcAuthServiceProvider;
    }

    public Provider<UserOidcAuthService> getUserOidcAuthServiceProvider() {
        return getUserOidcAuthServiceProvider;
    }

    @Inject
    public void setUserOidcAuthServiceProvider(Provider<UserOidcAuthService> oidcAuthServiceProvider) {
        this.getUserOidcAuthServiceProvider = oidcAuthServiceProvider;
    }
}
