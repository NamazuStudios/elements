package dev.getelements.elements.service.auth.oauth2;


import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.OAuth2AuthService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class OAuth2AuthServiceProvider implements Provider<OAuth2AuthService> {

    private User user;

    private Provider<AnonOAuth2AuthService> anonOAuth2AuthServiceProvider;

    private Provider<UserOAuth2AuthService> getUserOAuth2AuthServiceProvider;

    @Override
    public OAuth2AuthService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:
                return getUserOAuth2AuthServiceProvider().get();
            default:
                return getAnonOAuth2AuthServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonOAuth2AuthService> getAnonOAuth2AuthServiceProvider() {
        return anonOAuth2AuthServiceProvider;
    }

    @Inject
    public void setAnonOAuth2AuthServiceProvider(Provider<AnonOAuth2AuthService> anonOAuth2AuthServiceProvider) {
        this.anonOAuth2AuthServiceProvider = anonOAuth2AuthServiceProvider;
    }

    public Provider<UserOAuth2AuthService> getUserOAuth2AuthServiceProvider() {
        return getUserOAuth2AuthServiceProvider;
    }

    @Inject
    public void setUserOAuth2AuthServiceProvider(Provider<UserOAuth2AuthService> oAuth2AuthServiceProvider) {
        this.getUserOAuth2AuthServiceProvider = oAuth2AuthServiceProvider;
    }
}

