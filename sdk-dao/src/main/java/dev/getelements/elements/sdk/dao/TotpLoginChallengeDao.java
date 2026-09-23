package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.auth.TotpLoginChallenge;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * DAO for pending "password verified, awaiting TOTP code" login challenges. Mirrors the TTL-expiring,
 * single-use token pattern already used by {@code PasswordResetTokenDao} and the OIDC login attempt DAO.
 */
@ElementServiceExport
public interface TotpLoginChallengeDao {

    /**
     * Creates a new challenge, persisting the inputs needed to resume session creation once the code is
     * verified.
     *
     * @param userId the ID of the user who already supplied a valid password
     * @param profileId the originally-requested profile ID, or null
     * @param profileSelector the originally-requested profile selector query, or null
     * @param applicationNameOrId the originally-requested application name or ID, or null
     * @param expiry when this challenge stops being valid
     * @return the opaque challenge ID
     */
    String createChallenge(String userId, String profileId, String profileSelector, String applicationNameOrId, Timestamp expiry);

    /**
     * Finds a challenge by ID without consuming it, so an incorrect code (or trying a recovery code after a
     * mistyped TOTP code) can be retried against the same challenge rather than burning it on a failed
     * attempt. Also treats an already-expired challenge as absent, in case the TTL index hasn't reaped it yet.
     *
     * @param id the challenge ID
     * @return an {@link Optional} containing the challenge, or empty if unknown, already consumed, or expired
     */
    Optional<TotpLoginChallenge> find(String id);

    /**
     * Deletes a challenge by ID. Callers must only do this once the submitted code has actually verified
     * successfully -- consuming on a failed attempt would let one wrong guess permanently invalidate the
     * challenge for the legitimate follow-up attempt.
     *
     * @param id the challenge ID
     */
    void consume(String id);

}
