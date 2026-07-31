package dev.getelements.elements.sdk.model.session;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response body for {@code POST /oidc/session}. Exactly one of two shapes is populated: when a pending
 * browser-redirect attempt was started, {@link #getHandle()}, {@link #getAuthorizeUrl()}, and
 * {@link #getExpiresAt()} are set and {@link #getSession()} is {@code null}; when direct id_token validation
 * completed synchronously, only {@link #getSession()} is set.
 */
@Schema(description = "Either a pending login attempt (handle/authorizeUrl/expiresAt) " +
        "or a synchronously completed session, depending on whether an idToken was supplied in the request.")
public class OidcLoginAttemptResponse {

    /** Creates a new instance. */
    public OidcLoginAttemptResponse() {}

    @Schema(description = "The opaque handle used to poll GET /oidc/session/{handle}. Only set for pending attempts.")
    private String handle;

    @Schema(description = "The fully-built provider authorize URL to open in the system browser. " +
            "Only set for pending attempts.")
    private String authorizeUrl;

    @Schema(description = "The epoch second after which the attempt expires. Only set for pending attempts.")
    private Long expiresAt;

    @Schema(description = "The completed Elements session. Only set when idToken direct validation completed synchronously.")
    private SessionCreation session;

    /**
     * Builds a pending-attempt response.
     *
     * @param handle       the opaque poll handle
     * @param authorizeUrl the provider authorize URL
     * @param expiresAt    the epoch second expiry
     * @return the response
     */
    public static OidcLoginAttemptResponse pending(final String handle, final String authorizeUrl, final long expiresAt) {
        final var response = new OidcLoginAttemptResponse();
        response.setHandle(handle);
        response.setAuthorizeUrl(authorizeUrl);
        response.setExpiresAt(expiresAt);
        return response;
    }

    /**
     * Builds a synchronously-completed response.
     *
     * @param session the completed session
     * @return the response
     */
    public static OidcLoginAttemptResponse complete(final SessionCreation session) {
        final var response = new OidcLoginAttemptResponse();
        response.setSession(session);
        return response;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    public void setAuthorizeUrl(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public SessionCreation getSession() {
        return session;
    }

    public void setSession(SessionCreation session) {
        this.session = session;
    }

}
