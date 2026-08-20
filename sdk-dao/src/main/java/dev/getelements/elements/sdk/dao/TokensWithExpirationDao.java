package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.token.TokenWithExpiration;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.sql.Timestamp;

/**
 * Manipulates instances of {@link TokenWithExpiration} within the database.
 */

@ElementServiceExport
@ElementEventProducer(
        value = TokensWithExpirationDao.TOKEN_WITH_EXPIRATION_CREATED,
        parameters = TokenWithExpiration.class,
        description = "Called when a token with expiration was created."
)
@ElementEventProducer(
        value = TokensWithExpirationDao.TOKEN_WITH_EXPIRATION_CREATED,
        parameters = {TokenWithExpiration.class, Transaction.class},
        description = "Called when a token with expiration was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = TokensWithExpirationDao.TOKEN_WITH_EXPIRATION_DELETED,
        parameters = TokenWithExpiration.class,
        description = "Called when a token with expiration was deleted."
)
@ElementEventProducer(
        value = TokensWithExpirationDao.TOKEN_WITH_EXPIRATION_DELETED,
        parameters = {TokenWithExpiration.class, Transaction.class},
        description = "Called when a token with expiration was deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = TokensWithExpirationDao.TOKENS_WITH_EXPIRATION_TRUNCATED,
        parameters = User.class,
        description = "Called when all tokens with expiration for a user were deleted in bulk. Will not drive individual deletion events."
)
@ElementEventProducer(
        value = TokensWithExpirationDao.TOKENS_WITH_EXPIRATION_TRUNCATED,
        parameters = {User.class, Transaction.class},
        description = "Called when all tokens with expiration for a user were deleted in bulk. Will not drive individual deletion events. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface TokensWithExpirationDao {

    String TOKEN_WITH_EXPIRATION_CREATED = "dev.getelements.elements.sdk.model.dao.token.with.expiration.created";

    String TOKEN_WITH_EXPIRATION_DELETED = "dev.getelements.elements.sdk.model.dao.token.with.expiration.deleted";

    String TOKENS_WITH_EXPIRATION_TRUNCATED = "dev.getelements.elements.sdk.model.dao.tokens.with.expiration.truncated";
    /**
     * Creates an of {@link TokenWithExpiration}
     *
     * @param token
     * @return the token's id
     */
    String createToken(TokenWithExpiration token);

    /**
     * Given the token id, will return it's expiry value
     *
     * @param tokenId
     * @return
     */
    Timestamp getTokenExpiry(String tokenId);

    /**
     * Deletes all existing tokens associated with given user
     *
     * @param user
     */
    void deleteTokensByUser(User user);

    /**
     * Deletes the token using its id.
     *
     * @param tokenId
     */
    void deleteToken(String tokenId);
}
