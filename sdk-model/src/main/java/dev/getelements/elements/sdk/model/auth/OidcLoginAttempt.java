package dev.getelements.elements.sdk.model.auth;

import java.sql.Timestamp;

/**
 * Represents a single pending (or resolved) browser-redirect OIDC login attempt.
 *
 * <p>Not part of the public REST API — this is the DAO-level model consumed by the service layer to orchestrate
 * {@code POST /oidc/session}, {@code GET /oidc/session/{handle}}, and {@code GET /oidc/{provider}/callback}. The
 * {@code handle}, {@code state}, and {@code nonce} values are all server-generated via {@link java.security.SecureRandom}.
 */
public class OidcLoginAttempt {

    /** Creates a new instance. */
    public OidcLoginAttempt() {}

    /** The opaque bearer handle used to poll for completion. Doubles as the database primary key. */
    private String handle;

    /** The provider identifier this attempt was started for. */
    private String provider;

    /** The single-use CSRF state value bound to the authorize request and validated on callback. */
    private String state;

    /** The single-use nonce bound to the authorize request and validated against the resulting id_token. */
    private String nonce;

    /** The current lifecycle status. */
    private OidcLoginAttemptStatus status;

    /** The serialized {@code SessionCreation} payload, set on COMPLETE and cleared when claimed. */
    private String sessionToken;

    /** A human-readable failure reason, set on FAILED. */
    private String failureReason;

    /** The absolute timestamp after which this attempt is no longer valid. */
    private Timestamp expiry;

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public OidcLoginAttemptStatus getStatus() {
        return status;
    }

    public void setStatus(OidcLoginAttemptStatus status) {
        this.status = status;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

}
