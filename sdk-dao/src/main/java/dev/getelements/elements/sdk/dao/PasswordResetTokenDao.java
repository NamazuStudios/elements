package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.user.PasswordResetToken;
import dev.getelements.elements.sdk.model.user.User;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * DAO for single-use password reset tokens.
 */
@ElementServiceExport
@ElementEventProducer(
        value = PasswordResetTokenDao.PASSWORD_RESET_TOKEN_CREATED,
        parameters = PasswordResetToken.class,
        description = "Called when a password reset token was created."
)
@ElementEventProducer(
        value = PasswordResetTokenDao.PASSWORD_RESET_TOKEN_CREATED,
        parameters = {PasswordResetToken.class, Transaction.class},
        description = "Called when a password reset token was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = PasswordResetTokenDao.PASSWORD_RESET_TOKEN_DELETED,
        parameters = PasswordResetToken.class,
        description = "Called when a password reset token was deleted."
)
@ElementEventProducer(
        value = PasswordResetTokenDao.PASSWORD_RESET_TOKEN_DELETED,
        parameters = {PasswordResetToken.class, Transaction.class},
        description = "Called when a password reset token was deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = PasswordResetTokenDao.PASSWORD_RESET_TOKENS_TRUNCATED,
        parameters = User.class,
        description = "Called when all password reset tokens for a user were deleted in bulk. Will not drive individual deletion events."
)
@ElementEventProducer(
        value = PasswordResetTokenDao.PASSWORD_RESET_TOKENS_TRUNCATED,
        parameters = {User.class, Transaction.class},
        description = "Called when all password reset tokens for a user were deleted in bulk. Will not drive individual deletion events. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface PasswordResetTokenDao {

    String PASSWORD_RESET_TOKEN_CREATED = "dev.getelements.elements.sdk.model.dao.password.reset.token.created";

    String PASSWORD_RESET_TOKEN_DELETED = "dev.getelements.elements.sdk.model.dao.password.reset.token.deleted";

    String PASSWORD_RESET_TOKENS_TRUNCATED = "dev.getelements.elements.sdk.model.dao.password.reset.tokens.truncated";

    /**
     * Creates and persists a new password reset token for the given user.
     *
     * @param user   the user requesting the password reset
     * @param expiry the expiry timestamp after which the token is invalid
     * @return the opaque token string (also the database primary key)
     */
    String createToken(User user, Timestamp expiry);

    /**
     * Returns the token data if present and not expired.
     *
     * @param token the opaque token string
     * @return an {@link Optional} containing the token, or empty if absent or expired
     */
    Optional<PasswordResetToken> findToken(String token);

    /**
     * Deletes a token. Call after successful password reset to enforce single-use semantics.
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
