package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/**
 * A downstream application registered to authenticate against Elements as an OIDC relying party. Each
 * registration is a distinct OAuth 2.0/OIDC client: it carries the client identifier, a secret which is
 * only ever accepted on write and stored hashed, the exact redirect URIs it may use, the grant types it is
 * allowed to exercise, and the scopes it may request.
 *
 * <p>An optional link to an {@link dev.getelements.elements.sdk.model.application.Application} scopes the
 * resulting tokens to that application (driving profile resolution). Machine-to-machine clients which do not
 * act on behalf of a user have no application link.</p>
 */
@Schema(description = "A downstream application registered as an OIDC client of this Elements deployment.")
public class OidcClientRegistration {

    /** The default (and for 135a, only) token endpoint authentication method. */
    public static final String TOKEN_ENDPOINT_AUTH_METHOD_BASIC = "client_secret_basic";

    @Schema(description = "The unique client identifier presented by the relying party. Server-generated " +
            "when omitted on create; required on update.")
    private String clientId;

    @Schema(description = "The client secret. Accepted on write only; stored hashed and never returned.")
    private String clientSecret;

    @Schema(description = "A human-readable display name for this client registration.")
    @NotBlank
    private String displayName;

    @Schema(description = "The exact redirect URIs this client may use. Exact string matching per RFC 6819.")
    @NotEmpty
    private Set<String> redirectUris;

    @Schema(description = "The grant types this client may exercise.")
    @NotEmpty
    private Set<GrantType> grantTypes;

    @Schema(description = "The scopes this client may request.")
    @NotEmpty
    private Set<String> scopes;

    @Schema(description = "The registration status.")
    private Status status;

    @Schema(description = "Optional. The application id used to scope tokens issued to this client.")
    private String applicationId;

    @Schema(description = "When this registration was created, in milliseconds since the epoch.", readOnly = true)
    private long createdAt;

    public enum GrantType {
        /** The standard browser-redirect authorization code flow. */
        AUTHORIZATION_CODE,

        /** Refresh token exchange, rotating the access and refresh tokens under an existing grant. */
        REFRESH_TOKEN
    }

    public enum Status {
        /** The registration is usable. */
        ACTIVE,

        /** The registration exists but is refused at the authorization and token endpoints. */
        SUSPENDED
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Set<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(Set<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public Set<GrantType> getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(Set<GrantType> grantTypes) {
        this.grantTypes = grantTypes;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

}
