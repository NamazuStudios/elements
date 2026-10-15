package dev.getelements.elements.service.auth.oidc.provider;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.OidcClientService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.sdk.service.Services.forbidden;

public class OidcClientServiceProvider implements Provider<OidcClientService> {

    private User user;

    private Provider<SuperUserOidcClientService> superUserOidcClientService;

    @Override
    public OidcClientService get() {

        if (SUPERUSER.equals(user.getLevel())) {
            return getSuperUserOidcClientService().get();
        }

        return forbidden(OidcClientService.class);

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserOidcClientService> getSuperUserOidcClientService() {
        return superUserOidcClientService;
    }

    @Inject
    public void setSuperUserOidcClientService(Provider<SuperUserOidcClientService> superUserOidcClientService) {
        this.superUserOidcClientService = superUserOidcClientService;
    }

}
