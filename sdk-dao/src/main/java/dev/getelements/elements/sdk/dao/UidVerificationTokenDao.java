package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.user.UidVerificationToken;
import dev.getelements.elements.sdk.model.user.User;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * DAO for single-use email verification tokens used in the UserUid verification flow.
 */
@ElementServiceExport
@ElementEventProducer(
        value = UidVerificationTokenDao.UID_VERIFICATION_TOKEN_CREATED,
        parameters = UidVerificationToken.class,
        description = "Called when a UID verification token was created."
)
@ElementEventProducer(
        value = UidVerificationTokenDao.UID_VERIFICATION_TOKEN_CREATED,
        parameters = {UidVerificationToken.class, Transaction.class},
        description = "Called when a UID verification token was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = UidVerificationTokenDao.UID_VERIFICATION_TOKEN_DELETED,
        parameters = UidVerificationToken.class,
        description = "Called when a UID verification token was deleted."
)
@ElementEventProducer(
        value = UidVerificationTokenDao.UID_VERIFICATION_TOKEN_DELETED,
        parameters = {UidVerificationToken.class, Transaction.class},
        description = "Called when a UID verification token was deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = UidVerificationTokenDao.UID_VERIFICATION_TOKENS_TRUNCATED,
        parameters = User.class,
        description = "Called when all UID verification tokens for a user were deleted in bulk. Will not drive individual deletion events."
)
@ElementEventProducer(
        value = UidVerificationTokenDao.UID_VERIFICATION_TOKENS_TRUNCATED,
        parameters = {User.class, Transaction.class},
        description = "Called when all UID verification tokens for a user were deleted in bulk. Will not drive individual deletion events. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface UidVerificationTokenDao {

    String UID_VERIFICATION_TOKEN_CREATED = "dev.getelements.elements.sdk.model.dao.uid.verification.token.created";

    String UID_VERIFICATION_TOKEN_DELETED = "dev.getelements.elements.sdk.model.dao.uid.verification.token.deleted";

    String UID_VERIFICATION_TOKENS_TRUNCATED = "dev.getelements.elements.sdk.model.dao.uid.verification.tokens.truncated";

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
