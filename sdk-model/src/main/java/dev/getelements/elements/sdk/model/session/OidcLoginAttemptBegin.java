package dev.getelements.elements.sdk.model.session;

/** The result of starting a pending browser-redirect OIDC login attempt. Service-layer only. */
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

    private String id;

    private String authorizeUrl;

    private long expiresAt;

    private String confirmToken;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    public void setAuthorizeUrl(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getConfirmToken() {
        return confirmToken;
    }

    public void setConfirmToken(String confirmToken) {
        this.confirmToken = confirmToken;
    }

}
