package dev.getelements.elements.sdk.model.auth;

import java.sql.Timestamp;

/**
 * A long-lived grant issued by the Elements OIDC provider to a downstream client, tracking the user whose
 * identity was delegated, the scopes granted, and the Elements session currently backing the access token.
 *
 * <p>The grant is the anchor for the userinfo endpoint (scopes are looked up here rather than carried in the
 * access token, so the #134 token format stays untouched), for refresh-token rotation, and for revocation:
 * ending the backing session deletes the grant.</p>
 *
 * <p>Not part of the public REST API — this is the DAO-level model consumed by the provider's token and
 * userinfo services.</p>
 */
public class OidcProviderGrant {

    /** Creates a new instance. */
    public OidcProviderGrant() {}

    /** The opaque grant identifier. Doubles as the database primary key. */
    private String id;

    /** The client id of the registration this grant was issued to. */
    private String clientId;

    /** The id of the user the grant delegates. */
    private String userId;

    /** The optional application id the grant is scoped to. */
    private String applicationId;

    /** The scope string as granted, e.g. {@code "openid profile email"}. */
    private String scope;

    /** The id of the Elements session currently backing the access token. Uniquely indexed. */
    private String sessionId;

    /** The absolute timestamp after which this grant (and its refresh token) is no longer valid. */
    private Timestamp expiry;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

}
