package dev.getelements.elements.service.auth;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.AppleSignInAuthService;

import javax.inject.Inject;
import javax.inject.Provider;

public class AppleSignInAuthServiceProvider implements Provider<AppleSignInAuthService> {

    private User user;

    private Provider<AnonAppleSignInAuthService> anonAppleSignInAuthServiceProvider;

    private Provider<UserAppleSignInAuthService> userAppleSignInAuthServiceProvider;

    @Override
    public AppleSignInAuthService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:
                return getUserAppleSignInAuthServiceProvider().get();
            default:
                return getAnonAppleSignInAuthServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonAppleSignInAuthService> getAnonAppleSignInAuthServiceProvider() {
        return anonAppleSignInAuthServiceProvider;
    }

    @Inject
    public void setAnonAppleSignInAuthServiceProvider(Provider<AnonAppleSignInAuthService> anonAppleSignInAuthServiceProvider) {
        this.anonAppleSignInAuthServiceProvider = anonAppleSignInAuthServiceProvider;
    }

    public Provider<UserAppleSignInAuthService> getUserAppleSignInAuthServiceProvider() {
        return userAppleSignInAuthServiceProvider;
    }

    @Inject
    public void setUserAppleSignInAuthServiceProvider(Provider<UserAppleSignInAuthService> userAppleSignInAuthServiceProvider) {
        this.userAppleSignInAuthServiceProvider = userAppleSignInAuthServiceProvider;
    }

}
