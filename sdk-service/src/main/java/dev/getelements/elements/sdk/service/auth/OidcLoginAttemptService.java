package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptBegin;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptCallbackResult;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptStatusResponse;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Orchestrates the provider-agnostic browser-redirect OIDC login flow for thick clients: begin a pending
 * attempt, poll for completion, and handle the provider's callback. If the caller already has an Elements
 * session when calling {@link #begin}, a successful attempt links the external identity to that user instead
 * of creating/finding a user by external id; otherwise it behaves as a first-time, anonymous login.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface OidcLoginAttemptService {

    /**
     * Begins a pending login attempt for the given provider, building the provider's authorize URL and returning
     * an opaque id used to poll for completion. The success/error redirect URLs applied on callback, if any,
     * come from the provider's own configuration — server-authoritative, not caller-supplied. If the caller
     * already has an Elements session, the attempt links to that user on success rather than creating one.
     *
     * @param provider the provider identifier (e.g. "twitch")
     * @return the pending attempt's id, authorize URL, and expiry
     */
    OidcLoginAttemptBegin begin(String provider);

    /**
     * Polls a pending attempt by id. Returns COMPLETE with the session exactly once, on the poll that first
     * observes completion; every subsequent poll for the same id returns EXPIRED.
     *
     * @param id the opaque poll id
     * @return the current status of the attempt
     */
    OidcLoginAttemptStatusResponse poll(String id);

    /**
     * Finalizes an account-linking attempt by presenting the {@code confirmToken} returned only in the original
     * {@code begin()} response. The callback that validates the external identity is always hit by an
     * unauthenticated provider redirect and cannot verify it's talking to the same party that called
     * {@code begin()}, so the account-link mutation is deferred here instead, gated on possession of that secret.
     * Not applicable to (and never satisfied by) an anonymous attempt — see {@link #poll}, which rejects a
     * linking attempt outright and directs the caller here instead.
     *
     * @param id the opaque poll id
     * @param confirmToken the secret returned in the original {@code begin()} response for this attempt
     * @return the completed session, or an EXPIRED-equivalent result if the attempt is unknown, not awaiting
     *         confirmation, already claimed, or expired
     */
    OidcLoginAttemptStatusResponse confirmLink(String id, String confirmToken);

    /**
     * Handles the provider's redirect callback: looks up the pending attempt by state, exchanges the code for an
     * id_token, validates it, creates the Elements session, and marks the attempt COMPLETE. Fails closed (marks
     * the attempt FAILED) on any missing/expired/mismatched state, nonce mismatch, or exchange or validation
     * failure. Never throws for an expected failure mode — every outcome, success or failure, is reported via the
     * returned result so the REST layer can stay a thin dispatcher over it.
     *
     * @param provider the provider identifier from the callback path
     * @param code the authorization code from the provider, or {@code null} if the provider reported an error
     * @param state the state value from the provider, matched against the pending attempt
     * @param error the provider's {@code error} query parameter (e.g. user denied consent), or {@code null} on
     *              a normal code-bearing callback; when non-null, the code exchange is skipped and the attempt
     *              is marked FAILED directly with this value as the reason
     * @return the outcome, including the caller-configured redirect URL for that outcome, if any
     */
    OidcLoginAttemptCallbackResult handleCallback(String provider, String code, String state, String error);

}
