package dev.getelements.elements.service.auth.oauth2;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.OAuth2AuthSchemeService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.service.util.Services.forbidden;

public class OAuth2AuthSchemeServiceProvider implements Provider<OAuth2AuthSchemeService> {

    private User user;

    private Provider<SuperUserOAuth2AuthSchemeService> oAuth2AuthSchemeService;

    @Override
    public OAuth2AuthSchemeService get() {

        if (SUPERUSER.equals(user.getLevel())) {
            return getOAuth2AuthSchemeService().get();
        }

        return forbidden(OAuth2AuthSchemeService.class);

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserOAuth2AuthSchemeService> getOAuth2AuthSchemeService() {
        return oAuth2AuthSchemeService;
    }

    @Inject
    public void setOAuth2AuthSchemeService(Provider<SuperUserOAuth2AuthSchemeService> oAuth2AuthSchemeService) {
        this.oAuth2AuthSchemeService = oAuth2AuthSchemeService;
    }
}