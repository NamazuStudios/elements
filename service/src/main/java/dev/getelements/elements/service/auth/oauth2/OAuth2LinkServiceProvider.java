package dev.getelements.elements.service.auth.oauth2;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.OAuth2AuthService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class OAuth2LinkServiceProvider implements Provider<OAuth2AuthService> {

    private User user;

    private Provider<UserOAuth2AuthService> userOAuth2AuthServiceProvider;

    @Override
    public OAuth2AuthService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:
                return getUserOAuth2AuthServiceProvider().get();
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

    public Provider<UserOAuth2AuthService> getUserOAuth2AuthServiceProvider() {
        return userOAuth2AuthServiceProvider;
    }

    @Inject
    public void setUserOAuth2AuthServiceProvider(Provider<UserOAuth2AuthService> userOAuth2AuthServiceProvider) {
        this.userOAuth2AuthServiceProvider = userOAuth2AuthServiceProvider;
    }
}
