package dev.getelements.elements.sdk.model.oidc;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * The credential entry of the provider's hosted login page. The authorization parameters the page was opened
 * with are carried server-side via the login attempt's opaque id; this request carries only the user's
 * credentials and that id.
 */
@Schema(description = "The credentials submitted on the OIDC provider's hosted login page.")
public class OidcProviderLoginRequest {

    @Schema(description = "The opaque id of the pending authorization this login belongs to.")
    @NotBlank
    private String authorizationId;

    @Schema(description = "The user's username.")
    @NotBlank
    private String username;

    @Schema(description = "The user's password.")
    @NotBlank
    private String password;

    public String getAuthorizationId() {
        return authorizationId;
    }

    public void setAuthorizationId(String authorizationId) {
        this.authorizationId = authorizationId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
