package dev.getelements.elements.sdk.model.auth;

import java.sql.Timestamp;

/**
 * A single outstanding authorization code issued by the Elements OIDC provider. Modeled on
 * {@link OidcLoginAttempt}: the code is opaque, uniquely indexed, TTL-expired, and redeemable exactly once
 * via an atomic status-guarded transition.
 *
 * <p>Not part of the public REST API — this is the DAO-level model consumed by the provider's authorization
 * and token services.</p>
 */
public class OidcAuthorizationCode {

    /** Creates a new instance. */
    public OidcAuthorizationCode() {}

    /** The opaque single-use authorization code. Doubles as the database primary key. */
    private String code;

    /** The client id of the registration this code was issued to. */
    private String clientId;

    /** The id of the authenticated user the code was issued for. */
    private String userId;

    /** The optional application id the resulting tokens are scoped to. */
    private String applicationId;

    /** The redirect URI validated at the authorize endpoint and required again on exchange. */
    private String redirectUri;

    /** The scope string as granted, e.g. {@code "openid profile email"}. */
    private String scope;

    /** The client-generated CSRF state value, echoed on the completion redirect. */
    private String state;

    /** The nonce from the authorize request, echoed in the resulting id_token. */
    private String nonce;

    /** When the end-user authenticated, in milliseconds since the epoch. Carried as {@code auth_time}. */
    private long authTime;

    /** The PKCE code challenge from the authorize request, when present. */
    private String codeChallenge;

    /** The PKCE challenge method, when present. Always {@code S256} for 135a. */
    private String codeChallengeMethod;

    /** The current lifecycle status. */
    private Status status;

    /** The absolute timestamp after which this code is no longer redeemable. */
    private Timestamp expiry;

    public enum Status {
        /** The code has been issued but not yet redeemed. */
        PENDING,

        /** The user has authenticated but has not yet made a consent decision. */
        AWAITING_CONSENT,

        /** The end user denied the consent request. Any redemption attempt must fail. */
        DENIED,

        /** The code has been redeemed. Any further redemption attempt must fail. */
        REDEEMED
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
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

    public long getAuthTime() {
        return authTime;
    }

    public void setAuthTime(long authTime) {
        this.authTime = authTime;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public void setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(String codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

}
