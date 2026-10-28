package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * Payload of the event published when a user successfully authenticates via an OIDC provider, covering
 * the direct id_token login, the browser-redirect login, and the account-link flows. Carries everything
 * a plugin needs to act on the login -- including the token itself, so plugin code can call the
 * provider's API as the authenticated user.
 *
 * @param user the {@link User} which authenticated
 * @param profile the {@link Profile} attached to the resulting session, or null if the login requested
 *                no application profile
 * @param idToken the raw, validated id_token JWT used to authenticate, or null when the session was
 *                issued from an earlier-resolved identity (the login-attempt confirm flow), which has
 *                no live token
 * @param scheme the {@link OidcAuthScheme} the token was validated against, or null when unavailable
 *               (the login-attempt confirm flow)
 * @param provider the {@link OidcProviderConfiguration} of the provider used, or null for the direct
 *                 id_token path, which resolves the scheme from the token's issuer without a provider
 */
@Schema(description =
        "Payload of the event published when a user successfully authenticates via an OIDC provider.")
public record OidcAuthLoginEvent(

        @Schema(description = "The user which authenticated.")
        User user,

        @Schema(description = "The profile attached to the resulting session, if any.")
        Profile profile,

        @Schema(description = "The raw, validated id_token JWT used to authenticate, if available.")
        String idToken,

        @Schema(description = "The OIDC auth scheme the token was validated against, if available.")
        OidcAuthScheme scheme,

        @Schema(description = "The OIDC provider configuration used, if available.")
        OidcProviderConfiguration provider

) implements Serializable {}
