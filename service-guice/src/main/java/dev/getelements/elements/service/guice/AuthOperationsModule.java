package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;

import dev.getelements.elements.service.auth.oauth2.OAuth2AuthServiceOperations;
import dev.getelements.elements.service.auth.oauth2.OAuth2AuthServiceRequestInvoker;
import dev.getelements.elements.service.auth.oidc.OidcAuthServiceOperations;

public class AuthOperationsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(OAuth2AuthServiceOperations.class);
        bind(OAuth2AuthServiceRequestInvoker.class);

        bind(OidcAuthServiceOperations.class);
    }
}
