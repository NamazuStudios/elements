package dev.getelements.elements.dao;

import dev.getelements.elements.model.token.TokenWithExpiration;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

import java.sql.Timestamp;

/**
 * Manipulates instances of {@link TokenWithExpiration} within the database.
 *
 */
@Expose({
        @ModuleDefinition("eci.elements.dao.expiration_tokens")
})
public interface TokensWithExpirationDao {
    /**
     * Creates an of {@link TokenWithExpiration}
     * @param token
     * @return the token's id
     */
    String createToken(TokenWithExpiration token);

    /**
     * Given the token id, will return it's expiry value
     * @param tokenId
     * @return
     */
    Timestamp getTokenExpiry(String tokenId);

    /**
     * Deletes all existing tokens associated with given user
     * @param user
     */
    void deleteTokensByUser(User user);

    /**
     * Deletes the token using its id.
     * @param tokenId
     */
    void deleteToken(String tokenId);
}
