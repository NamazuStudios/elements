package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.token.TokenWithExpiration;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.sql.Timestamp;

/**
 * Manipulates instances of {@link TokenWithExpiration} within the database.
 */

@ElementServiceExport
public interface TokensWithExpirationDao {
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
