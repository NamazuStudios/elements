package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.OidcAuthService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class OidcLinkServiceProvider implements Provider<OidcAuthService> {

    private User user;

    private Provider<UserOidcAuthService> userOidcAuthServiceProvider;

    @Override
    public OidcAuthService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:
                return getUserOidcAuthServiceProvider().get();
            default:
                throw new ForbiddenException("Authentication required to link accounts.");
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserOidcAuthService> getUserOidcAuthServiceProvider() {
        return userOidcAuthServiceProvider;
    }

    @Inject
    public void setUserOidcAuthServiceProvider(Provider<UserOidcAuthService> userOidcAuthServiceProvider) {
        this.userOidcAuthServiceProvider = userOidcAuthServiceProvider;
    }
}
