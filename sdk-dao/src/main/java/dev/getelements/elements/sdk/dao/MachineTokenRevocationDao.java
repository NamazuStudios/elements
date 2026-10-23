package dev.getelements.elements.sdk.dao;

import java.sql.Timestamp;

/**
 * Stores revoked machine-token ids.
 *
 * <p>Machine tokens are stateless JWTs, so revocation is a denylist: only revoked token ids are
 * persisted, keyed by the token's {@code jti}, and each entry expires with the token. Verification
 * consults this store, which is the one database lookup per request that a stateless token cannot
 * avoid.</p>
 *
 * <p>Like {@link OidcProviderDao}, this DAO is internal server infrastructure and is deliberately
 * not exported to Elements.</p>
 */
public interface MachineTokenRevocationDao {

    /**
     * Revokes the supplied token id, retaining the entry until the token's natural expiry. Revoking
     * the same id again is idempotent and extends the entry to the later expiry.
     *
     * @param tokenId the token's {@code jti}
     * @param expiry when the token naturally expires
     */
    void revoke(String tokenId, Timestamp expiry);

    /**
     * Returns true if the supplied token id is currently revoked and unexpired.
     *
     * @param tokenId the token's {@code jti}
     * @return true if the token is revoked
     */
    boolean isRevoked(String tokenId);

}
