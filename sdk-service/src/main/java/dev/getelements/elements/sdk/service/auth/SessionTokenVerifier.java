package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.auth.SessionTokenClaims;

/**
 * Verifies Elements-issued session tokens. Implementations validate the token's signature, expiry,
 * issuer, and audience against the server's own signing keys, and return the resolved
 * {@link SessionTokenClaims} on success. Any failure to verify results in an
 * {@link dev.getelements.elements.sdk.model.exception.UnauthorizedException}.
 *
 * This is the mechanism by which the system distinguishes its own signed session tokens from legacy
 * opaque session secrets, which continue to resolve through
 * {@link dev.getelements.elements.sdk.dao.SessionDao#getBySessionSecret(String)} during the migration
 * window.
 */
@ElementServiceExport
public interface SessionTokenVerifier {

    /**
     * Verifies the supplied session token, returning its resolved {@link SessionTokenClaims}.
     *
     * @param sessionToken the session token to verify
     * @return the resolved claims
     */
    SessionTokenClaims verify(String sessionToken);

    /**
     * Returns true if the supplied credential has the shape of a session token, as opposed to a legacy
     * opaque session secret. Shape alone does not imply validity; a shaped credential must still verify.
     *
     * @param credentials the credential to inspect
     * @return true if the credential appears to be a session token
     */
    boolean isSessionToken(String credentials);

}
