package dev.getelements.elements.dao.mongo.model.auth;

import dev.getelements.elements.sdk.model.auth.OidcRefreshToken;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.Property;

import java.sql.Timestamp;

/**
 * MongoDB entity for a provider refresh token. Only the SHA-256 hex digest of the presented token is stored —
 * the token itself is high-entropy, so an unsalted digest is safe (cf. {@code TotpRecoveryCodeHasher}) and,
 * unlike a salted digest, supports lookup-by-hash. The unique index is on the digest. The TTL index on
 * {@code expiry} removes expired tokens.
 */
@Entity(value = "oidc_refresh_token", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("secretHash"), options = @IndexOptions(unique = true)),
        @Index(fields = @Field(value = "expiry"), options = @IndexOptions(expireAfterSeconds = 0))
})
public class MongoOidcRefreshToken {

    @Id
    private String id;

    @Property
    private String secretHash;

    @Property
    private String grantId;

    @Property
    private String clientId;

    @Property
    private OidcRefreshToken.Status status;

    @Property
    private Timestamp expiry;

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

    public OidcRefreshToken.Status getStatus() {
        return status;
    }

    public void setStatus(OidcRefreshToken.Status status) {
        this.status = status;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

}
