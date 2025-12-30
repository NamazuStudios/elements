package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.OidcAuthSchemeService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.service.util.Services.forbidden;

public class OidcAuthSchemeServiceProvider implements Provider<OidcAuthSchemeService> {

    private User user;

    private Provider<SuperUserOidcAuthSchemeService> oidcAuthSchemeService;

    @Override
    public OidcAuthSchemeService get() {

        if (SUPERUSER.equals(user.getLevel())) {
            return getOidcAuthSchemeService().get();
        }

        return forbidden(OidcAuthSchemeService.class);

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserOidcAuthSchemeService> getOidcAuthSchemeService() {
        return oidcAuthSchemeService;
    }

    @Inject
    public void setOidcAuthSchemeService(Provider<SuperUserOidcAuthSchemeService> oidcAuthSchemeService) {
        this.oidcAuthSchemeService = oidcAuthSchemeService;
    }
}
