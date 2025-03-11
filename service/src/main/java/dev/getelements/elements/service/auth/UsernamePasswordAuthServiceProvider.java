package dev.getelements.elements.service.auth;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.auth.UsernamePasswordAuthService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * Created by patricktwohig on 4/2/15.
 */
public class UsernamePasswordAuthServiceProvider implements Provider<UsernamePasswordAuthService> {

    private User user;

    private Provider<UserUsernamePasswordAuthService> userAuthServiceProvider;

    private Provider<AnonUsernamePasswordAuthService> anonAuthServiceProvider;

    @Override
    public UsernamePasswordAuthService get() {
        switch (getUser().getLevel()) {
            case UNPRIVILEGED:
                return getAnonAuthServiceProvider().get();
            default:
                return getUserAuthServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserUsernamePasswordAuthService> getUserAuthServiceProvider() {
        return userAuthServiceProvider;
    }

    @Inject
    public void setUserAuthServiceProvider(Provider<UserUsernamePasswordAuthService> userAuthServiceProvider) {
        this.userAuthServiceProvider = userAuthServiceProvider;
    }

    public Provider<AnonUsernamePasswordAuthService> getAnonAuthServiceProvider() {
        return anonAuthServiceProvider;
    }

    @Inject
    public void setAnonAuthServiceProvider(Provider<AnonUsernamePasswordAuthService> anonAuthServiceProvider) {
        this.anonAuthServiceProvider = anonAuthServiceProvider;
    }

}
