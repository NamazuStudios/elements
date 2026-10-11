package dev.getelements.elements.dao.mongo.model.auth;

import dev.getelements.elements.sdk.model.auth.JwtSigningKey;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

import java.util.Map;

/**
 * The persisted form of the server's own session-token signing key. The private half is stored
 * encrypted at rest; {@code kid} is the primary key and is carried in every issued token's JOSE
 * header.
 */
@Entity(value = "jwt_signing_key", useDiscriminator = false)
public class MongoJwtSigningKey {

    @Id
    private String kid;

    private String algorithm;

    private JwtSigningKey.Status status;

    private String publicKey;

    private String privateKey;

    private Map<String, Object> encryption;

    private long createdAt;

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public JwtSigningKey.Status getStatus() {
        return status;
    }

    public void setStatus(JwtSigningKey.Status status) {
        this.status = status;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public Map<String, Object> getEncryption() {
        return encryption;
    }

    public void setEncryption(Map<String, Object> encryption) {
        this.encryption = encryption;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

}
