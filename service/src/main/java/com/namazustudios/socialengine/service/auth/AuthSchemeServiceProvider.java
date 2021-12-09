package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class AuthSchemeServiceProvider implements Provider<AuthSchemeService> {

    private User user;

    private Provider<SuperUserAuthSchemeService> authSchemeService;

    @Override
    public AuthSchemeService get() {
        return getAuthSchemeService().get();
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserAuthSchemeService> getAuthSchemeService() {
        return authSchemeService;
    }

    @Inject
    public void setAuthSchemeService(Provider<SuperUserAuthSchemeService> authSchemeService) {
        this.authSchemeService = authSchemeService;
    }
}
