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
     */
    public OidcLoginAttemptBegin(final String id, final String authorizeUrl, final long expiresAt) {
        this.id = id;
        this.authorizeUrl = authorizeUrl;
        this.expiresAt = expiresAt;
    }

    private String id;

    private String authorizeUrl;

    private long expiresAt;

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

}
