package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.auth.TotpUserState;

import java.util.List;
import java.util.Optional;

/**
 * DAO for a single user's TOTP enrollment state (shared secret, enabled flag, hashed recovery codes). Kept
 * separate from {@link UserDao} because this state is credential material, mirroring how the password
 * hash/salt are kept off the public {@link dev.getelements.elements.sdk.model.user.User} model entirely --
 * this DAO's {@link TotpUserState} return type is DAO-level only, never exposed via REST directly.
 */
@ElementServiceExport
public interface TotpDao {

    /**
     * Finds the current TOTP enrollment state for a user, if any enrollment (pending or confirmed) exists.
     *
     * @param userId the user ID
     * @return an {@link Optional} containing the state, or empty if the user has never begun enrollment
     */
    Optional<TotpUserState> findState(String userId);

    /**
     * Begins (or restarts) enrollment for a user: stores a new secret, not yet enforced until confirmed.
     * Overwrites any previous pending or confirmed enrollment.
     *
     * @param userId the user ID
     * @param secret the newly-generated Base32 shared secret
     * @return the resulting (not-yet-enabled) state
     */
    TotpUserState beginEnrollment(String userId, String secret);

    /**
     * Confirms a pending enrollment, activating enforcement and storing the given hashed recovery codes.
     *
     * @param userId the user ID
     * @param hashedRecoveryCodes the hashed recovery codes to store
     * @return the resulting (enabled) state
     */
    TotpUserState confirmEnrollment(String userId, List<String> hashedRecoveryCodes);

    /**
     * Disables and clears TOTP enrollment for a user -- the secret and any remaining recovery codes are
     * removed entirely, so a subsequent enrollment starts fresh.
     *
     * @param userId the user ID
     */
    void disable(String userId);

    /**
     * Atomically consumes a recovery code if it is present among the user's remaining hashed codes.
     *
     * @param userId the user ID
     * @param hashedCode the hashed recovery code to look up
     * @return true if the code was present (and has now been consumed), false otherwise
     */
    boolean consumeRecoveryCode(String userId, String hashedCode);

}
