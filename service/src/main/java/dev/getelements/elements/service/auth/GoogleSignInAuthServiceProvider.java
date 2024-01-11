package dev.getelements.elements.service.auth;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.GoogleSignInAuthService;

import javax.inject.Inject;
import javax.inject.Provider;

public class GoogleSignInAuthServiceProvider implements Provider<GoogleSignInAuthService> {

    private User user;

    private Provider<AnonGoogleSignInAuthService> anonGoogleSignInAuthServiceProvider;

    private Provider<GoogleSignInAuthService> googleSignInAuthServiceProvider;

    @Override
    public GoogleSignInAuthService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:
                return getGoogleSignInAuthServiceProvider().get();
            default:
                return getAnonGoogleSignInAuthServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonGoogleSignInAuthService> getAnonGoogleSignInAuthServiceProvider() {
        return anonGoogleSignInAuthServiceProvider;
    }

    @Inject
    public void setAnonGoogleSignInAuthServiceProvider(Provider<AnonGoogleSignInAuthService> anonGoogleSignInAuthServiceProvider) {
        this.anonGoogleSignInAuthServiceProvider = anonGoogleSignInAuthServiceProvider;
    }

    public Provider<GoogleSignInAuthService> getGoogleSignInAuthServiceProvider() {
        return googleSignInAuthServiceProvider;
    }

    @Inject
    public void setGoogleSignInAuthServiceProvider(Provider<GoogleSignInAuthService> userGoogleSignInAuthServiceProvider) {
        this.googleSignInAuthServiceProvider = userGoogleSignInAuthServiceProvider;
    }
}
