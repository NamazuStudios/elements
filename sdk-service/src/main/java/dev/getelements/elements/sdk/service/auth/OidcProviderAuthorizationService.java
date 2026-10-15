package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.auth.OidcAuthorizationCode;
import dev.getelements.elements.sdk.model.oidc.OidcProviderAuthorizationRequest;
import dev.getelements.elements.sdk.model.oidc.OidcProviderConsent;
import dev.getelements.elements.sdk.model.oidc.OidcProviderLoginRequest;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

/**
 * Orchestrates the authorization-code flow's front half: validating the authorize request and producing the
 * single-use authorization code that the token endpoint later redeems, plus the hosted login and consent
 * steps that attach an authenticated user to it.
 *
 * <p>Two entries produce a code: the hosted login page (credentials submitted on Elements' own page), and an
 * already-authenticated Elements session (skipping the login page entirely). When consent is required for the
 * deployment, the login step only attaches the authenticated user; the code completes once the user approves
 * the consent screen, or is irrevocably denied.</p>
 */
@ElementServiceExport
public interface OidcProviderAuthorizationService {

    /**
     * Validates the authorize request against the client's registration (existence and status, exact redirect
     * URI match, allowed scopes, PKCE requirements) and creates a PENDING authorization code awaiting user
     * authentication.
     *
     * @param request the authorize request's query parameters
     * @return the created PENDING code record; its {@code code} value identifies the pending authorization to
     *         the hosted login page
     */
    OidcAuthorizationCode beginAuthorization(OidcProviderAuthorizationRequest request);

    /**
     * Authenticates the submitted credentials against the pending authorization, attaching the user and
     * {@code auth_time} to the code record. When the deployment requires consent, the returned outcome asks
     * the caller to present the consent screen; otherwise the redirect URI carrying the {@code code} and
     * {@code state} is returned immediately.
     *
     * @param loginRequest the credentials submitted on the hosted login page
     * @return the outcome: a redirect URI, or the consent details to render
     */
    OidcProviderConsent submitLogin(OidcProviderLoginRequest loginRequest);

    /**
     * Records the end user's approval of the pending consent and completes the authorization, returning the
     * redirect URI carrying the {@code code} and the client's {@code state}.
     *
     * @param authorizationId the pending authorization's opaque id
     * @return the redirect URI to send the browser to
     */
    String approveConsent(String authorizationId);

    /**
     * Records the end user's denial of the pending consent, permanently refusing the code, and returns the
     * redirect URI carrying an RFC 6749 {@code access_denied} error plus the client's {@code state}.
     *
     * @param authorizationId the pending authorization's opaque id
     * @return the redirect URI to send the browser to
     */
    String denyConsent(String authorizationId);

}
