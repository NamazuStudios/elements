package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.auth.OidcLoginAttempt;

import java.util.Optional;

/**
 * DAO for pending browser-redirect OIDC login attempts.
 *
 * <p>All status transitions below are implemented as atomic, conditional updates (guarded on the current status)
 * so that a replayed callback delivery or a concurrent double-poll cannot observe or create a session twice.
 */
@ElementServiceExport
public interface OidcLoginAttemptDao {

    /**
     * Creates and persists a new PENDING attempt.
     *
     * @param attempt the attempt to create, with {@code handle}, {@code provider}, {@code state}, {@code nonce},
     *                and {@code expiry} already populated
     * @return the created attempt
     */
    OidcLoginAttempt create(OidcLoginAttempt attempt);

    /**
     * Finds a PENDING attempt by provider and state, without mutating it. Used by the callback handler before
     * performing the token exchange. Returns empty if the attempt is missing, expired, not PENDING, or belongs
     * to a different provider than the one supplied.
     *
     * @param provider the provider slug from the callback path
     * @param state the state value from the callback query parameter
     * @return an {@link Optional} containing the attempt, or empty
     */
    Optional<OidcLoginAttempt> findPendingByState(String provider, String state);

    /**
     * Atomically transitions a PENDING attempt matching the given state to COMPLETE, storing the serialized
     * session. A no-op (empty result) if no PENDING attempt matches — this is what makes a replayed callback
     * delivery fail closed instead of creating a second session.
     *
     * @param state the state value
     * @param sessionCreationJson the serialized {@code SessionCreation} payload
     * @return an {@link Optional} containing the updated attempt, or empty if the guard did not match
     */
    Optional<OidcLoginAttempt> markComplete(String state, String sessionCreationJson);

    /**
     * Atomically transitions a PENDING attempt matching the given state to FAILED. A no-op (empty result) if no
     * PENDING attempt matches.
     *
     * @param state the state value
     * @param reason a human-readable failure reason
     * @return an {@link Optional} containing the updated attempt, or empty if the guard did not match
     */
    Optional<OidcLoginAttempt> markFailed(String state, String reason);

    /**
     * Atomically claims a COMPLETE attempt matching the given handle, transitioning it to a terminal claimed
     * state and clearing the stored session so it cannot be observed again. Returns the pre-claim attempt (with
     * the session still populated) to the caller that wins the race; a no-op (empty result) if no COMPLETE
     * attempt matches — including on every subsequent call for the same handle.
     *
     * @param handle the opaque poll handle
     * @return an {@link Optional} containing the pre-claim attempt, or empty if already claimed, not yet
     *         complete, expired, or unknown
     */
    Optional<OidcLoginAttempt> claimCompleteByHandle(String handle);

    /**
     * Finds a non-COMPLETE attempt by handle without mutating it, for reporting PENDING or FAILED status during
     * a poll. Returns empty if the attempt is missing, expired, COMPLETE (use {@link #claimCompleteByHandle}
     * instead), or already claimed.
     *
     * @param handle the opaque poll handle
     * @return an {@link Optional} containing the attempt, or empty
     */
    Optional<OidcLoginAttempt> findPendingOrFailedByHandle(String handle);

}
