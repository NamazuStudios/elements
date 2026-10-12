package dev.getelements.elements.dao.mongo.model.auth;

import dev.getelements.elements.sdk.model.auth.OidcClientRegistration;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;

import java.util.Set;

/**
 * MongoDB entity for a downstream OIDC client registration. The {@code clientId} is the document {@code _id},
 * so uniqueness is enforced by the primary key. The client secret is stored only as a salted digest; the raw
 * secret is never persisted. Registrations are a small, admin-owned collection and are looked up by
 * {@code clientId}; no secondary indexes are needed.
 */
@Entity(value = "oidc_client", useDiscriminator = false)
public class MongoOidcClient {

    @Id
    private String clientId;

    @Property
    private String displayName;

    @Property
    private byte[] secretSalt;

    @Property
    private byte[] secretHash;

    @Property
    private String hashAlgorithm;

    @Property
    private Set<String> redirectUris;

    @Property
    private Set<OidcClientRegistration.GrantType> grantTypes;

    @Property
    private Set<String> scopes;

    @Property
    private OidcClientRegistration.Status status;

    @Property
    private String applicationId;

    @Property
    private long createdAt;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte[] getSecretSalt() {
        return secretSalt;
    }

    public void setSecretSalt(byte[] secretSalt) {
        this.secretSalt = secretSalt;
    }

    public byte[] getSecretHash() {
        return secretHash;
    }

    public void setSecretHash(byte[] secretHash) {
        this.secretHash = secretHash;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public Set<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(Set<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public Set<OidcClientRegistration.GrantType> getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(Set<OidcClientRegistration.GrantType> grantTypes) {
        this.grantTypes = grantTypes;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public OidcClientRegistration.Status getStatus() {
        return status;
    }

    public void setStatus(OidcClientRegistration.Status status) {
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
