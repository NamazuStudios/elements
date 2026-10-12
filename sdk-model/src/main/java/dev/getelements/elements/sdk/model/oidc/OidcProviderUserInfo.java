package dev.getelements.elements.sdk.model.oidc;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The claims returned by the provider's userinfo endpoint. Claims are gated by the scopes granted to the
 * client's grant: {@code sub} is always present, {@code name} and {@code preferred_username} require the
 * {@code profile} scope, and {@code email} requires the {@code email} scope. Absent claims are simply omitted
 * from the response.
 */
@Schema(description = "The claims returned by the OIDC provider's userinfo endpoint.")
public class OidcProviderUserInfo {

    @Schema(description = "The subject: the Elements user id.")
    private String sub;

    @Schema(description = "The user's display name. Requires the profile scope.")
    private String name;

    @Schema(description = "The user's Elements username. Requires the profile scope.")
    private String preferredUsername;

    @Schema(description = "The user's email address. Requires the email scope.")
    private String email;

    @Schema(description = "Whether the email address is verified. Requires the email scope.")
    private Boolean emailVerified;

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

}
