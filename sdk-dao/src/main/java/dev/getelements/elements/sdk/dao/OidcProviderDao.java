package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.auth.OidcAuthorizationCode;
import dev.getelements.elements.sdk.model.auth.OidcClientRegistration;
import dev.getelements.elements.sdk.model.auth.OidcProviderGrant;
import dev.getelements.elements.sdk.model.auth.OidcRefreshToken;

import java.util.List;
import java.util.Optional;

/**
 * Persists the Elements OIDC provider's server-side state: downstream client registrations, outstanding
 * authorization codes, active grants, and refresh tokens. Like {@link JwtSigningKeyDao}, this DAO is internal
 * server infrastructure and is deliberately not exported to Elements.
 *
 * <p>The authorization-code and refresh-token operations are single-use by contract: redemption and rotation
 * are implemented as atomic, status-guarded transitions so a replayed code or a concurrently presented refresh
 * token cannot produce a second grant.</p>
 */
public interface OidcProviderDao {

    //
    // Client registrations
    //

    /**
     * Creates and persists a new client registration. The registration's {@code clientSecret} is hashed
     * before persistence; the returned registration never carries it.
     *
     * @param registration the registration to create
     * @return the created registration
     */
    OidcClientRegistration createClient(OidcClientRegistration registration);

    /**
     * Updates an existing client registration. If {@code clientSecret} is non-null it is re-hashed and
     * replaced; if null the existing secret hash is retained.
     *
     * @param registration the registration to update, with {@code clientId} identifying the record
     * @return the updated registration
     */
    OidcClientRegistration updateClient(OidcClientRegistration registration);

    /**
     * Deletes a client registration.
     *
     * @param clientId the client id of the registration to delete
     */
    void deleteClient(String clientId);

    /**
     * Fetches a client registration by client id.
     *
     * @param clientId the client id
     * @return an {@link Optional} containing the registration, or empty
     */
    Optional<OidcClientRegistration> getClient(String clientId);

    /**
     * Returns all client registrations.
     *
     * @return all registrations
     */
    List<OidcClientRegistration> getClients();

    /**
     * Verifies a presented client secret against the registration's stored salted digest, in constant time.
     * Convenience for the token endpoint's client authentication.
     *
     * @param clientId the client id of the registration
     * @param clientSecret the presented secret
     * @return true if the registration exists and the secret matches
     */
    boolean verifyClientSecret(String clientId, String clientSecret);

    //
    // Authorization codes
    //

    /**
     * Creates and persists a new PENDING authorization code.
     *
     * @param code the code to create, with {@code code}, {@code expiry}, and the authorization context
     *             already populated
     * @return the created code
     */
    OidcAuthorizationCode createAuthorizationCode(OidcAuthorizationCode code);

    /**
     * Atomically redeems a PENDING, unexpired authorization code by transitioning it to REDEEMED. A second
     * redemption attempt of the same code returns empty — the caller must treat that as an invalid grant and,
     * per RFC 6749 section 4.1.2, revoke all tokens previously issued on that code.
     *
     * @param code the presented code value
     * @return an {@link Optional} containing the redeemed code record, or empty if unknown, expired, not
     *         PENDING, or already redeemed
     */
    Optional<OidcAuthorizationCode> redeemAuthorizationCode(String code);

    /**
     * Atomically attaches the authenticated user to a PENDING, unexpired authorization code that is still
     * awaiting login (its user id unset), recording when authentication occurred. A second completion attempt
     * returns empty.
     *
     * @param code the pending authorization's code value
     * @param userId the id of the authenticated user
     * @param authTime when the user authenticated, in milliseconds since the epoch
     * @return an {@link Optional} containing the updated code record, or empty if unknown, expired, not
     *         PENDING, or already carrying a user
     */
    Optional<OidcAuthorizationCode> completeAuthorizationCode(String code, String userId, long authTime);

    //
    // Grants
    //

    /**
     * Creates and persists a new grant.
     *
     * @param grant the grant to create, with {@code id} and {@code sessionId} already populated
     * @return the created grant
     */
    OidcProviderGrant createGrant(OidcProviderGrant grant);

    /**
     * Fetches a grant by its id.
     *
     * @param grantId the grant id
     * @return an {@link Optional} containing the grant, or empty if unknown or expired
     */
    Optional<OidcProviderGrant> getGrant(String grantId);

    /**
     * Returns every unexpired grant the supplied user holds with the supplied client. Used by end-session to
     * revoke all of a user's grants for one downstream application.
     *
     * @param userId the user id
     * @param clientId the client id
     * @return the matching grants, possibly empty
     */
    List<OidcProviderGrant> getGrantsForUserAndClient(String userId, String clientId);

    /**
     * Fetches a grant by the id of the Elements session currently backing its access token.
     *
     * @param sessionId the session id carried in the access token's {@code sid} claim
     * @return an {@link Optional} containing the grant, or empty
     */
    Optional<OidcProviderGrant> findGrantBySessionId(String sessionId);

    /**
     * Moves a grant's backing session from the old session to the new one, used when a refresh exchange
     * rotates the access token. The previous session should be revoked by the caller.
     *
     * @param grantId the grant id
     * @param oldSessionId the session id currently recorded on the grant
     * @param newSessionId the session id of the freshly issued access token
     * @return an {@link Optional} containing the updated grant, or empty if the grant is missing, expired, or
     *         no longer carries {@code oldSessionId}
     */
    Optional<OidcProviderGrant> rotateGrantSession(String grantId, String oldSessionId, String newSessionId);

    /**
     * Deletes the grant backed by the supplied session id, if present. Used when the backing session ends.
     *
     * @param sessionId the session id
     */
    void deleteGrantBySessionId(String sessionId);

    //
    // Refresh tokens
    //

    /**
     * Creates and persists a new ACTIVE refresh token. The supplied {@code secretHash} must already be
     * computed by the caller; the raw token is never persisted.
     *
     * @param refreshToken the token to create
     * @return the created token
     */
    OidcRefreshToken createRefreshToken(OidcRefreshToken refreshToken);

    /**
     * Atomically consumes an ACTIVE, unexpired refresh token by transitioning it to CONSUMED, keyed on the
     * token's secret hash. A replayed token returns empty.
     *
     * @param secretHash the hash of the presented refresh token
     * @return an {@link Optional} containing the consumed token record, or empty if unknown, expired, or
     *         already consumed
     */
    Optional<OidcRefreshToken> consumeRefreshToken(String secretHash);

    /**
     * Revokes every refresh token belonging to the supplied grant.
     *
     * @param grantId the grant id
     */
    void revokeRefreshTokensForGrant(String grantId);

}
