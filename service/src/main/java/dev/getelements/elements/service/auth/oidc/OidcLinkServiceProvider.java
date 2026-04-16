package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.OidcLinkService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class OidcLinkServiceProvider implements Provider<OidcLinkService> {

    private User user;

    private Provider<AnonOidcLinkService> anonServiceProvider;

    private Provider<UserOidcAuthService> userOidcAuthServiceProvider;

    @Override
    public OidcLinkService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:
                return getUserOidcAuthServiceProvider().get();
            default:
                return getAnonServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonOidcLinkService> getAnonServiceProvider() {
        return anonServiceProvider;
    }

    @Inject
    public void setAnonServiceProvider(Provider<AnonOidcLinkService> anonServiceProvider) {
        this.anonServiceProvider = anonServiceProvider;
    }

    public Provider<UserOidcAuthService> getUserOidcAuthServiceProvider() {
        return userOidcAuthServiceProvider;
    }

    @Inject
    public void setUserOidcAuthServiceProvider(Provider<UserOidcAuthService> userOidcAuthServiceProvider) {
        this.userOidcAuthServiceProvider = userOidcAuthServiceProvider;
    }
}
