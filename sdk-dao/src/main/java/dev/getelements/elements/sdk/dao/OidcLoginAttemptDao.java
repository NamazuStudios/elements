package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
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
@ElementEventProducer(
        value = OidcLoginAttemptDao.OIDC_LOGIN_ATTEMPT_CREATED,
        parameters = OidcLoginAttempt.class,
        description = "Called when an OIDC login attempt was created."
)
@ElementEventProducer(
        value = OidcLoginAttemptDao.OIDC_LOGIN_ATTEMPT_CREATED,
        parameters = {OidcLoginAttempt.class, Transaction.class},
        description = "Called when an OIDC login attempt was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = OidcLoginAttemptDao.OIDC_LOGIN_ATTEMPT_UPDATED,
        parameters = OidcLoginAttempt.class,
        description = "Called when an OIDC login attempt transitioned status (e.g. marked complete, marked failed, or claimed)."
)
@ElementEventProducer(
        value = OidcLoginAttemptDao.OIDC_LOGIN_ATTEMPT_UPDATED,
        parameters = {OidcLoginAttempt.class, Transaction.class},
        description = "Called when an OIDC login attempt transitioned status. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface OidcLoginAttemptDao {

    String OIDC_LOGIN_ATTEMPT_CREATED = "dev.getelements.elements.sdk.model.dao.oidc.login.attempt.created";

    String OIDC_LOGIN_ATTEMPT_UPDATED = "dev.getelements.elements.sdk.model.dao.oidc.login.attempt.updated";

    /**
     * Creates and persists a new PENDING attempt.
     *
     * @param attempt the attempt to create, with {@code id}, {@code provider}, {@code state}, {@code nonce},
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
     * Atomically transitions a PENDING attempt matching the given state to LINK_READY, storing the serialized
     * external-identity claims validated by the callback for a linking attempt. A no-op (empty result) if no
     * PENDING attempt matches — mirrors {@link #markComplete}'s replay-safety guarantee. Unlike
     * {@link #markComplete}, this deliberately does not create a session; the account-link mutation is deferred
     * to {@link #claimLinkReadyById}, gated on presenting the attempt's {@code confirmToken}.
     *
     * @param state the state value
     * @param linkClaimsJson the serialized external-identity claims
     * @return an {@link Optional} containing the updated attempt, or empty if the guard did not match
     */
    Optional<OidcLoginAttempt> markLinkReady(String state, String linkClaimsJson);

    /**
     * Finds a LINK_READY attempt by id, without mutating it. Used both to detect that a plain poll should be
     * rejected in favor of the confirm flow, and by the confirm flow itself to check {@code confirmToken} before
     * consuming anything — a wrong token must never burn the attempt.
     *
     * @param id the opaque poll id
     * @return an {@link Optional} containing the attempt, or empty if missing, expired, or not LINK_READY
     */
    Optional<OidcLoginAttempt> findLinkReadyById(String id);

    /**
     * Atomically claims a LINK_READY attempt matching the given id, transitioning it to a terminal claimed state
     * and clearing the stored claims so it cannot be observed again. Returns the pre-claim attempt (with
     * {@code linkClaimsJson} still populated) to the caller that wins the race; a no-op (empty result) if no
     * LINK_READY attempt matches — including on every subsequent call for the same id. Callers must have already
     * verified {@code confirmToken} before calling this — it performs no token check of its own.
     *
     * @param id the opaque poll id
     * @return an {@link Optional} containing the pre-claim attempt, or empty if already claimed, not yet
     *         link-ready, expired, or unknown
     */
    Optional<OidcLoginAttempt> claimLinkReadyById(String id);

    /**
     * Atomically claims a COMPLETE attempt matching the given id, transitioning it to a terminal claimed
     * state and clearing the stored session so it cannot be observed again. Returns the pre-claim attempt (with
     * the session still populated) to the caller that wins the race; a no-op (empty result) if no COMPLETE
     * attempt matches — including on every subsequent call for the same id.
     *
     * @param id the opaque poll id
     * @return an {@link Optional} containing the pre-claim attempt, or empty if already claimed, not yet
     *         complete, expired, or unknown
     */
    Optional<OidcLoginAttempt> claimCompleteById(String id);

    /**
     * Finds a non-COMPLETE attempt by id without mutating it, for reporting PENDING or FAILED status during
     * a poll. Returns empty if the attempt is missing, expired, COMPLETE (use {@link #claimCompleteById}
     * instead), or already claimed.
     *
     * @param id the opaque poll id
     * @return an {@link Optional} containing the attempt, or empty
     */
    Optional<OidcLoginAttempt> findPendingOrFailedById(String id);

}
