package dev.getelements.elements.dao;

import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

@Expose({
        @ModuleDefinition("eci.elements.dao.reset_password")
})
public interface ResetPasswordTokensDao {
    String insertPasswordResetToken(int expiry, String userId, String email);
    int getTokenExpiry(String tokenId);
    void removeTokensForEmail(String email);
    void removeTokenById(String tokenId);
}
