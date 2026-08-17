package dev.getelements.elements.sdk.model.auth;

import java.sql.Timestamp;

/**
 * Represents a single pending (or resolved) browser-redirect OIDC login attempt.
 *
 * <p>Not part of the public REST API — this is the DAO-level model consumed by the service layer to orchestrate
 * {@code POST /oidc/session}, {@code GET /oidc/session/{id}}, and {@code GET /oidc/{provider}/callback}. The
 * {@code id}, {@code state}, and {@code nonce} values are all server-generated via {@link java.security.SecureRandom}.
 */
public class OidcLoginAttempt {

    /** Creates a new instance. */
    public OidcLoginAttempt() {}

    /** The opaque bearer token used to poll for completion. Doubles as the database primary key. */
    private String id;

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

    /** Optional. If set, the callback redirects the browser here on success instead of the default HTML page. */
    private String successRedirectUrl;

    /** Optional. If set, the callback redirects the browser here on failure instead of the default HTML page. */
    private String errorRedirectUrl;

    /**
     * The id of the already-authenticated user this attempt was started on behalf of, set at {@code begin()}
     * time when the caller had an existing Elements session. When set, a successful callback links the external
     * identity to this user instead of creating/finding a user by external id. {@code null} for an anonymous
     * (first-time login) attempt.
     */
    private String linkedUserId;

    /**
     * Serialized external-identity claims (scheme name, external user id, email, profile claims) validated by
     * the callback for a linking attempt, set when transitioning to {@link OidcLoginAttemptStatus#LINK_READY}.
     * Consumed exactly once, by the authenticated poll that performs the deferred account-link mutation.
     */
    private String linkClaimsJson;

    /**
     * A random, high-entropy token generated at {@code begin()} time and returned only in that response — never
     * exposed to the browser/IdP leg of the flow the way {@code state}/{@code nonce} are. Presenting it back on
     * {@code POST .../confirm} is the sole proof that the caller finalizing a linking attempt is the same party
     * that started it, since the callback that validates the external identity is always hit by an
     * unauthenticated provider redirect and cannot make that determination itself.
     */
    private String confirmToken;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getSuccessRedirectUrl() {
        return successRedirectUrl;
    }

    public void setSuccessRedirectUrl(String successRedirectUrl) {
        this.successRedirectUrl = successRedirectUrl;
    }

    public String getErrorRedirectUrl() {
        return errorRedirectUrl;
    }

    public void setErrorRedirectUrl(String errorRedirectUrl) {
        this.errorRedirectUrl = errorRedirectUrl;
    }

    public String getLinkedUserId() {
        return linkedUserId;
    }

    public void setLinkedUserId(String linkedUserId) {
        this.linkedUserId = linkedUserId;
    }

    public String getLinkClaimsJson() {
        return linkClaimsJson;
    }

    public void setLinkClaimsJson(String linkClaimsJson) {
        this.linkClaimsJson = linkClaimsJson;
    }

    public String getConfirmToken() {
        return confirmToken;
    }

    public void setConfirmToken(String confirmToken) {
        this.confirmToken = confirmToken;
    }

}
