package dev.getelements.elements.service.auth;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.FirebaseAuthService;

import javax.inject.Inject;
import javax.inject.Provider;

public class FirebaseAuthServiceProvider implements Provider<FirebaseAuthService> {

    private User user;

    private Provider<AnonFirebaseAuthService> anonFirebaseAuthServiceProvider;

    private Provider<UserFirebaseAuthService> userFirebaseAuthServiceProvider;

    @Override
    public FirebaseAuthService get() {
        switch (getUser().getLevel()) {
            case USER:
            case SUPERUSER:
                return getUserFirebaseAuthServiceProvider().get();
            default:
                return getAnonFirebaseAuthServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonFirebaseAuthService> getAnonFirebaseAuthServiceProvider() {
        return anonFirebaseAuthServiceProvider;
    }

    @Inject
    public void setAnonFirebaseAuthServiceProvider(Provider<AnonFirebaseAuthService> anonFirebaseAuthServiceProvider) {
        this.anonFirebaseAuthServiceProvider = anonFirebaseAuthServiceProvider;
    }

    public Provider<UserFirebaseAuthService> getUserFirebaseAuthServiceProvider() {
        return userFirebaseAuthServiceProvider;
    }

    @Inject
    public void setUserFirebaseAuthServiceProvider(Provider<UserFirebaseAuthService> userFirebaseAuthServiceProvider) {
        this.userFirebaseAuthServiceProvider = userFirebaseAuthServiceProvider;
    }

}
