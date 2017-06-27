package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.AuthService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 4/2/15.
 */
public class AuthServiceProvider implements Provider<AuthService> {

    private User user;

    private Provider<UserAuthService> userAuthServiceProvider;

    private Provider<AnonAuthService> anonAuthServiceProvider;

    @Override
    public AuthService get() {
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

    public Provider<UserAuthService> getUserAuthServiceProvider() {
        return userAuthServiceProvider;
    }

    @Inject
    public void setUserAuthServiceProvider(Provider<UserAuthService> userAuthServiceProvider) {
        this.userAuthServiceProvider = userAuthServiceProvider;
    }

    public Provider<AnonAuthService> getAnonAuthServiceProvider() {
        return anonAuthServiceProvider;
    }

    @Inject
    public void setAnonAuthServiceProvider(Provider<AnonAuthService> anonAuthServiceProvider) {
        this.anonAuthServiceProvider = anonAuthServiceProvider;
    }

}
