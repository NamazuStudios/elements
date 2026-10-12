package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.oidc.OidcProviderDiscoveryDocument;

/**
 * Produces the Elements OIDC provider's {@code /.well-known/openid-configuration} discovery document. The
 * {@code issuer} is configuration-driven ({@code Constants.OIDC_PROVIDER_ISSUER}) and every endpoint URL is
 * resolved against it, so deployments behind proxies or custom paths only need to set that one property.
 */
public interface OidcProviderDiscoveryService {

    /**
     * Builds the discovery document.
     *
     * @return the discovery document
     */
    OidcProviderDiscoveryDocument getDiscoveryDocument();

}
