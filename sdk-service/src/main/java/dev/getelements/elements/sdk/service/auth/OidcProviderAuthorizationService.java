package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.model.auth.OidcAuthorizationCode;
import dev.getelements.elements.sdk.model.oidc.OidcProviderAuthorizationRequest;
import dev.getelements.elements.sdk.model.oidc.OidcProviderLoginRequest;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

/**
 * Orchestrates the authorization-code flow's front half: validating the authorize request and producing the
 * single-use authorization code that the token endpoint later redeems.
 *
 * <p>Two entries produce a code: the hosted login page (credentials submitted on Elements' own page), and an
 * already-authenticated Elements session (skipping the login page entirely). In both cases the returned
 * redirect URI carries the {@code code} and the client's {@code state}.</p>
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
     * Authenticates the submitted credentials against the pending authorization and completes its front half:
     * the user id and {@code auth_time} are attached to the code record, and the redirect URI carrying the
     * {@code code} and {@code state} is returned.
     *
     * @param loginRequest the credentials submitted on the hosted login page
     * @return the redirect URI to send the browser to
     */
    String completeAuthorization(OidcProviderLoginRequest loginRequest);

}
