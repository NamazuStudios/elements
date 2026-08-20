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
     * The callback validated an external identity for a linking attempt (one with
     * {@link OidcLoginAttempt#getLinkedUserId()} set), but has deliberately not yet performed the account-link
     * mutation. The callback itself is always hit by an unauthenticated provider redirect, so it cannot verify
     * it's talking to the same caller who started the attempt; only an authenticated poll whose session matches
     * {@link OidcLoginAttempt#getLinkedUserId()} may complete the link. Distinct from {@link #COMPLETE}, which
     * carries an already-created session ready to be claimed as-is.
     */
    LINK_READY,

    /**
     * The completed session has already been claimed by a poller, or a link-ready attempt has already been
     * consumed by a poll. A terminal state distinct from {@link #COMPLETE}/{@link #LINK_READY} so a second poll
     * cannot observe either a second time.
     */
    CLAIMED

}
