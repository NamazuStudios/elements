package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.auth.JWKSet;

/**
 * Publishes the public halves of the Elements OIDC provider's signing keys as a {@link JWKSet}. The keys
 * are the same keys the #134 session-token issuer signs with, so key rotation is reflected here without
 * redeploy and downstream verifiers cannot drift from the session-token infrastructure.
 */
public interface OidcProviderJwksService {

    /**
     * Returns the current JWKS, containing every public signing key (active and retired) so tokens issued
     * before a rotation continue to verify.
     *
     * @return the JWKS
     */
    JWKSet getJwks();

}
