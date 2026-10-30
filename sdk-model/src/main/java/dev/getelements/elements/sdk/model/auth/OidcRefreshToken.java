package dev.getelements.elements.sdk.model.auth;

import java.sql.Timestamp;

/**
 * A refresh token issued by the Elements OIDC provider, presented at the token endpoint to rotate the access
 * and refresh tokens under an existing {@link OidcProviderGrant}. The token itself is never stored — only a
 * hash, uniquely indexed for lookup, so a database leak does not expose usable credentials.
 *
 * <p>Not part of the public REST API — this is the DAO-level model consumed by the provider's token service.</p>
 */
public class OidcRefreshToken {

    /** Creates a new instance. */
    public OidcRefreshToken() {}

    /** The opaque token identifier. Doubles as the database primary key. */
    private String id;

    /** The hex-encoded hash of the presented token. Uniquely indexed; the raw token is never stored. */
    private String secretHash;

    /** The id of the {@link OidcProviderGrant} this token rotates. */
    private String grantId;

    /** The client id of the registration this token was issued to. */
    private String clientId;

    /** The current lifecycle status. */
    private Status status;

    /** The absolute timestamp after which this token is no longer valid. */
    private Timestamp expiry;

    public enum Status {
        /** The token may be presented for a refresh exchange. */
        ACTIVE,

        /** The token has been consumed by a rotation or revoked; presentation must fail. */
        CONSUMED
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecretHash() {
        return secretHash;
    }

    public void setSecretHash(String secretHash) {
        this.secretHash = secretHash;
    }

    public String getGrantId() {
        return grantId;
    }

    public void setGrantId(String grantId) {
        this.grantId = grantId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
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
