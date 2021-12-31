package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.UsernamePasswordAuthService;

import javax.inject.Inject;
import javax.inject.Provider;

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
