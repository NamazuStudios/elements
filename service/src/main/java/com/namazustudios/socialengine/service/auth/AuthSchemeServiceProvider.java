package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.model.user.User;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class AuthSchemeServiceProvider implements Provider<AuthSchemeService> {

    private User user;

    private Provider<SuperUserAuthSchemeService> superUserAuthSchemeServiceProvider;

    @Override
    public AuthSchemeService get() {

        if (getUser().getLevel() == User.Level.SUPERUSER) {
            return getSuperUserAuthSchemeServiceProvider().get();
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

    public Provider<SuperUserAuthSchemeService> getSuperUserAuthSchemeServiceProvider() {
        return superUserAuthSchemeServiceProvider;
    }

    @Inject
    public void setSuperUserAuthSchemeServiceProvider(Provider<SuperUserAuthSchemeService> superUserAuthSchemeServiceProvider) {
        this.superUserAuthSchemeServiceProvider = superUserAuthSchemeServiceProvider;
    }
}
