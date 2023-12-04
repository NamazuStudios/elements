package dev.getelements.elements.dao;

import dev.getelements.elements.model.token.TokenWithExpiration;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

@Expose({
        @ModuleDefinition("eci.elements.dao.expiration_tokens")
})
public interface TokensWithExpirationDao {
    String insertToken(TokenWithExpiration token);
    int getTokenExpiry(String tokenId);
    void removeTokensForEmail(String email);
    void removeTokenById(String tokenId);
}
