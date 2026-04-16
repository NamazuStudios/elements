package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.service.auth.OidcAuthService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class OidcAuthServiceProvider implements Provider<OidcAuthService> {

    private Provider<AnonOidcAuthService> anonOidcAuthServiceProvider;

    @Override
    public OidcAuthService get() {
        return getAnonOidcAuthServiceProvider().get();
    }

    public Provider<AnonOidcAuthService> getAnonOidcAuthServiceProvider() {
        return anonOidcAuthServiceProvider;
    }

    @Inject
    public void setAnonOidcAuthServiceProvider(Provider<AnonOidcAuthService> anonOidcAuthServiceProvider) {
        this.anonOidcAuthServiceProvider = anonOidcAuthServiceProvider;
    }
}
