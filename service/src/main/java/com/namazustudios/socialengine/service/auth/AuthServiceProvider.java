package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.AuthService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 4/2/15.
 */
public class AuthServiceProvider implements Provider<AuthService> {

    @Inject
    private User user;

    @Inject
    private Provider<UserAuthService> userAuthServiceProvider;

    @Inject Provider<AnonAuthService> anonAuthServiceProvider;

    @Override
    public AuthService get() {
        switch (user.getLevel()) {
            case UNPRIVILEGED:
                return anonAuthServiceProvider.get();
            default:
                return userAuthServiceProvider.get();
        }
    }

}
