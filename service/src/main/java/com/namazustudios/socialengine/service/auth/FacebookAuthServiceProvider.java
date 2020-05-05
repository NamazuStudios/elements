package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.FacebookAuthService;

import javax.inject.Inject;
import javax.inject.Provider;

public class FacebookAuthServiceProvider implements Provider<FacebookAuthService> {

    private User user;

    private Provider<AnonFacebookAuthService> anonFacebookAuthServiceProvider;

    private Provider<UserFacebookAuthService> userFacebookAuthServiceProvider;

    @Override
    public FacebookAuthService get() {
        switch (getUser().getLevel()) {
            case USER:
                return getUserFacebookAuthServiceProvider().get();
            default:
                return getAnonFacebookAuthServiceProvider().get();
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<AnonFacebookAuthService> getAnonFacebookAuthServiceProvider() {
        return anonFacebookAuthServiceProvider;
    }

    @Inject
    public void setAnonFacebookAuthServiceProvider(Provider<AnonFacebookAuthService> anonFacebookAuthServiceProvider) {
        this.anonFacebookAuthServiceProvider = anonFacebookAuthServiceProvider;
    }

    public Provider<UserFacebookAuthService> getUserFacebookAuthServiceProvider() {
        return userFacebookAuthServiceProvider;
    }

    @Inject
    public void setUserFacebookAuthServiceProvider(Provider<UserFacebookAuthService> userFacebookAuthServiceProvider) {
        this.userFacebookAuthServiceProvider = userFacebookAuthServiceProvider;
    }
}
