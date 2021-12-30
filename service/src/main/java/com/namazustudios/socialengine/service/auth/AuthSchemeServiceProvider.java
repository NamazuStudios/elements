package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.model.user.User.Level.SUPERUSER;
import static com.namazustudios.socialengine.service.Services.forbidden;

public class AuthSchemeServiceProvider implements Provider<AuthSchemeService> {

    private User user;

    private Provider<SuperUserAuthSchemeService> authSchemeService;

    @Override
    public AuthSchemeService get() {

        if (SUPERUSER.equals(user.getLevel())) {
            return getAuthSchemeService().get();
        }

        return forbidden(AuthSchemeService.class);

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
