package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.user.UidVerificationToken;
import dev.getelements.elements.sdk.model.user.User;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * DAO for single-use email verification tokens used in the UserUid verification flow.
 */
@ElementServiceExport
public interface UidVerificationTokenDao {

    /**
     * Creates and persists a new token for the given user and UID.
     *
     * @param user   the user that owns the UID
     * @param scheme the scheme of the UID to verify
     * @param uidId  the id value of the UID to verify
     * @param expiry the expiry timestamp after which the token is invalid
     * @return the opaque token string (also the database primary key)
     */
    String createToken(User user, String scheme, String uidId, Timestamp expiry);

    /**
     * Returns the token data if present and not expired.
     *
     * @param token the opaque token string
     * @return an {@link Optional} containing the token, or empty if absent or expired
     */
    Optional<UidVerificationToken> findToken(String token);

    /**
     * Deletes a token. Call after successful verification to enforce single-use semantics.
     *
     * @param token the opaque token string to delete
     */
    void deleteToken(String token);

    /**
     * Deletes all tokens belonging to the given user. Call on user deletion.
     *
     * @param user the user whose tokens should be removed
     */
    void deleteTokensByUser(User user);

}
