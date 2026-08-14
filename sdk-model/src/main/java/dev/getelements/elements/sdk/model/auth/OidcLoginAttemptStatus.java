package dev.getelements.elements.sdk.model.auth;

/**
 * The internal lifecycle status of an {@link OidcLoginAttempt}. Not exposed directly via the public API — see
 * {@code OidcLoginAttemptState} in the session model package for the API-facing view.
 */
public enum OidcLoginAttemptStatus {

    /** Waiting on the provider's redirect/callback. */
    PENDING,

    /** The callback completed successfully; the session is available to be claimed exactly once. */
    COMPLETE,

    /** The callback failed (denial, validation failure, exchange failure). */
    FAILED,

    /**
     * The completed session has already been claimed by a poller. A terminal state distinct from
     * {@link #COMPLETE} so a second poll cannot observe the session a second time.
     */
    CLAIMED

}
