package dev.getelements.elements.sdk.model.session;

import io.swagger.v3.oas.annotations.media.Schema;

/** The result of starting a pending browser-redirect OIDC login attempt. Service-layer only. */
@Schema(description = "The result of starting a pending browser-redirect OIDC login attempt.")
public class OidcLoginAttemptBegin {

    /** Creates a new instance. */
    public OidcLoginAttemptBegin() {}

    /**
     * Creates a new instance with all fields.
     *
     * @param id the opaque poll id
     * @param authorizeUrl the fully-built provider authorize URL
     * @param expiresAt the epoch second after which the attempt expires
     * @param confirmToken a secret returned only to the caller of {@code begin()}, presented back on
     *                     {@code POST .../confirm} to finalize an account-linking attempt. Present for every
     *                     attempt, but only ever required for a linking one.
     */
    public OidcLoginAttemptBegin(final String id, final String authorizeUrl, final long expiresAt,
                                  final String confirmToken) {
        this.id = id;
        this.authorizeUrl = authorizeUrl;
        this.expiresAt = expiresAt;
        this.confirmToken = confirmToken;
    }

    @Schema(description = "The opaque id used to poll GET /oidc/session/{id}.")
    private String id;

    @Schema(description = "The fully-built provider authorize URL to open in the system browser.")
    private String authorizeUrl;

    @Schema(description = "The epoch second after which the attempt expires.")
    private long expiresAt;

    @Schema(description = "A secret returned only to the caller of begin(), presented back on " +
            "POST .../confirm to finalize an account-linking attempt.")
    private String confirmToken;

    /**
     * Returns the opaque poll id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the opaque poll id.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the fully-built provider authorize URL.
     *
     * @return the authorize URL
     */
    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    /**
     * Sets the fully-built provider authorize URL.
     *
     * @param authorizeUrl the authorize URL
     */
    public void setAuthorizeUrl(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }

    /**
     * Returns the epoch second after which the attempt expires.
     *
     * @return the expiry
     */
    public long getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets the epoch second after which the attempt expires.
     *
     * @param expiresAt the expiry
     */
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Returns the secret used to finalize a linking attempt.
     *
     * @return the confirmToken
     */
    public String getConfirmToken() {
        return confirmToken;
    }

    /**
     * Sets the secret used to finalize a linking attempt.
     *
     * @param confirmToken the confirmToken
     */
    public void setConfirmToken(String confirmToken) {
        this.confirmToken = confirmToken;
    }

}
