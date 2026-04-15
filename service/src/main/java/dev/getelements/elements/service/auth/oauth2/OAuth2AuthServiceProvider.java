package dev.getelements.elements.service.auth.oauth2;


import dev.getelements.elements.sdk.service.auth.OAuth2AuthService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class OAuth2AuthServiceProvider implements Provider<OAuth2AuthService> {

    private Provider<AnonOAuth2AuthService> anonOAuth2AuthServiceProvider;

    @Override
    public OAuth2AuthService get() {
        return getAnonOAuth2AuthServiceProvider().get();
    }

    public Provider<AnonOAuth2AuthService> getAnonOAuth2AuthServiceProvider() {
        return anonOAuth2AuthServiceProvider;
    }

    @Inject
    public void setAnonOAuth2AuthServiceProvider(Provider<AnonOAuth2AuthService> anonOAuth2AuthServiceProvider) {
        this.anonOAuth2AuthServiceProvider = anonOAuth2AuthServiceProvider;
    }
}
