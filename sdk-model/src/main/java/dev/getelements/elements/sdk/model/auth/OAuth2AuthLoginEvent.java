package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Map;

/**
 * Payload of the event published when a user successfully authenticates via an OAuth2 auth scheme,
 * covering both the login and account-link flows. Carries everything a plugin needs to act on the
 * login -- including the credential material the client presented, so plugin code can call the
 * provider's API using the same token.
 *
 * @param user the {@link User} which authenticated
 * @param profile the {@link Profile} attached to the resulting session, or null if the login resolved
 *                no profile
 * @param scheme the {@link OAuth2AuthScheme} used to validate the presented credential
 * @param requestParameters the credential material presented by the client (the request parameters and
 *                          headers, merged), including the access token used for validation
 */
@Schema(description =
        "Payload of the event published when a user successfully authenticates via an OAuth2 auth scheme.")
public record OAuth2AuthLoginEvent(

        @Schema(description = "The user which authenticated.")
        User user,

        @Schema(description = "The profile attached to the resulting session, if any.")
        Profile profile,

        @Schema(description = "The OAuth2 auth scheme used to validate the credential.")
        OAuth2AuthScheme scheme,

        @Schema(description = "The credential material presented by the client (merged request parameters and headers).")
        Map<String, String> requestParameters

) implements Serializable {}
