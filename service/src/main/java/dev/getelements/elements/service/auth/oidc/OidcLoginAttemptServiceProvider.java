package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.OidcLoginAttemptService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * Dispatches on the caller's access level, mirroring {@link OidcLinkServiceProvider} — an already-authenticated
 * caller (USER/SUPERUSER) gets an account-linking attempt via {@link UserOidcLoginAttemptService}; anyone else
 * gets the anonymous, first-time-login attempt via {@link AnonOidcLoginAttemptService}.
 */
public class OidcLoginAttemptServiceProvider implements Provider<OidcLoginAttemptService> {

    private User user;

    private Provider<AnonOidcLoginAttemptService> anonOidcLoginAttemptServiceProvider;

    private Provider<UserOidcLoginAttemptService> userOidcLoginAttemptServiceProvider;

    @Override
    public OidcLoginAttemptService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:
                return getUserOidcLoginAttemptServiceProvider().get();
            default:
                return getAnonOidcLoginAttemptServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonOidcLoginAttemptService> getAnonOidcLoginAttemptServiceProvider() {
        return anonOidcLoginAttemptServiceProvider;
    }

    @Inject
    public void setAnonOidcLoginAttemptServiceProvider(Provider<AnonOidcLoginAttemptService> anonOidcLoginAttemptServiceProvider) {
        this.anonOidcLoginAttemptServiceProvider = anonOidcLoginAttemptServiceProvider;
    }

    public Provider<UserOidcLoginAttemptService> getUserOidcLoginAttemptServiceProvider() {
        return userOidcLoginAttemptServiceProvider;
    }

    @Inject
    public void setUserOidcLoginAttemptServiceProvider(Provider<UserOidcLoginAttemptService> userOidcLoginAttemptServiceProvider) {
        this.userOidcLoginAttemptServiceProvider = userOidcLoginAttemptServiceProvider;
    }

}
