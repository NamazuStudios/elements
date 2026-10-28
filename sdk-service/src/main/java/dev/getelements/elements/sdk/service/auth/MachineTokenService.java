package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.auth.MachineToken;
import dev.getelements.elements.sdk.model.auth.MachineTokenClaims;
import dev.getelements.elements.sdk.model.auth.MachineTokenRequest;

/**
 * Mints and verifies signed machine tokens for non-user principals.
 *
 * <p>A machine token is an RS256 JWT signed with this deployment's active OIDC signing key, so it is
 * verifiable against the published JWKS and carries the same {@code iss} as the OIDC provider. Unlike
 * a {@code client_credentials} token, the caller supplies the subject, audience, lifetime, and any
 * domain-specific claims, and the token can be revoked before it expires.</p>
 *
 * <p>This is an in-process service for trusted Elements. It deliberately knows nothing about the
 * caller's domain: an Element maps its own concepts (organizations, pods, capabilities, and so on)
 * onto custom claims and uses the returned token id to revoke the token later.</p>
 */
@ElementServiceExport
public interface MachineTokenService {

    /**
     * Mints a signed machine token.
     *
     * @param request the subject, audience, lifetime, and custom claims
     * @return the signed token and its decoded claims
     */
    MachineToken issue(MachineTokenRequest request);

    /**
     * Verifies a signed machine token: signature, expiry, issuer, and the expected audience, followed
     * by the revocation denylist.
     *
     * @param token the compact JOSE token to verify
     * @param expectedAudience the audience the caller requires the token to carry
     * @return the verified claims
     * @throws dev.getelements.elements.sdk.model.exception.UnauthorizedException if the token is
     *         malformed, expired, revoked, or carries the wrong issuer or audience
     */
    MachineTokenClaims verify(String token, String expectedAudience);

    /**
     * Revokes a machine token so it stops verifying before its natural expiry. Revocation is
     * idempotent and is scoped to the single token id.
     *
     * @param tokenId the {@code jti} of the token to revoke
     * @param expiresAtMillis when the token naturally expires, in milliseconds since the epoch; the
     *        denylist entry is retained until then
     */
    void revoke(String tokenId, long expiresAtMillis);

}
