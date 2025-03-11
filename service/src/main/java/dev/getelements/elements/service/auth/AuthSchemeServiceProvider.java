package dev.getelements.elements.service.auth;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.auth.AuthSchemeService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.service.util.Services.forbidden;

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
