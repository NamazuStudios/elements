package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptBegin;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptCallbackResult;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptStatusResponse;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Orchestrates the pre-authentication, provider-agnostic browser-redirect OIDC login flow for thick clients: begin
 * a pending attempt, poll for completion, and handle the provider's callback. Always anonymous — a caller has no
 * Elements session yet when using this service, by definition.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface OidcLoginAttemptService {

    /**
     * Begins a pending login attempt for the given provider, building the provider's authorize URL and returning
     * an opaque handle used to poll for completion.
     *
     * @param provider the provider identifier (e.g. "twitch")
     * @param successRedirectUrl if set, {@code GET /oidc/{provider}/callback} redirects the browser here on
     *                           success instead of rendering the default success page; may be {@code null}
     * @param errorRedirectUrl if set, {@code GET /oidc/{provider}/callback} redirects the browser here on
     *                         failure instead of rendering the default error page; may be {@code null}
     * @return the pending attempt's handle, authorize URL, and expiry
     */
    OidcLoginAttemptBegin begin(String provider, String successRedirectUrl, String errorRedirectUrl);

    /**
     * Polls a pending attempt by handle. Returns COMPLETE with the session exactly once, on the poll that first
     * observes completion; every subsequent poll for the same handle returns EXPIRED.
     *
     * @param handle the opaque poll handle
     * @return the current status of the attempt
     */
    OidcLoginAttemptStatusResponse poll(String handle);

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
