package dev.getelements.elements.service.auth.oidc.provider;

import dev.getelements.elements.sdk.model.oidc.OidcProviderDiscoveryDocument;
import dev.getelements.elements.sdk.service.auth.OidcProviderDiscoveryService;
import dev.getelements.elements.sdk.service.Constants;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.Set;

import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.CODE_CHALLENGE_METHOD_S256;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.GRANT_TYPE_AUTHORIZATION_CODE;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.GRANT_TYPE_REFRESH_TOKEN;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.RESPONSE_TYPE_CODE;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.SCOPE_EMAIL;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.SCOPE_OPENID;
import static dev.getelements.elements.sdk.model.oidc.OidcProviderProtocol.SCOPE_PROFILE;

public class StandardOidcProviderDiscoveryService implements OidcProviderDiscoveryService {

    @Inject
    @Named(Constants.OIDC_PROVIDER_ISSUER)
    private String issuer;

    @Override
    public OidcProviderDiscoveryDocument getDiscoveryDocument() {

        final var base = getIssuer();

        if (base.endsWith("/")) {
            throw new IllegalStateException("Configured OIDC provider issuer must not end with a slash: " + base);
        }

        final var document = new OidcProviderDiscoveryDocument();

        document.setIssuer(base);
        document.setAuthorizationEndpoint(base + "/oidc/authorize");
        document.setTokenEndpoint(base + "/oidc/token");
        document.setUserinfoEndpoint(base + "/oidc/userinfo");
        document.setEndSessionEndpoint(base + "/oidc/end-session");
        document.setJwksUri(base + "/.well-known/jwks.json");

        document.setGrantTypesSupported(Set.of(GRANT_TYPE_AUTHORIZATION_CODE, GRANT_TYPE_REFRESH_TOKEN));
        document.setScopesSupported(Set.of(SCOPE_OPENID, SCOPE_PROFILE, SCOPE_EMAIL));
        document.setIdTokenSigningAlgValuesSupported(Set.of("RS256"));
        document.setResponseTypesSupported(Set.of(RESPONSE_TYPE_CODE));
        document.setCodeChallengeMethodsSupported(Set.of(CODE_CHALLENGE_METHOD_S256));

        return document;

    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(@Named(Constants.OIDC_PROVIDER_ISSUER) String issuer) {
        this.issuer = issuer;
    }

}
