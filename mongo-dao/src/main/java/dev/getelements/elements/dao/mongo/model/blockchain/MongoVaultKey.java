package dev.getelements.elements.dao.mongo.model.blockchain;

import dev.getelements.elements.model.crypto.PrivateKeyCrytpoAlgorithm;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import java.util.Map;

@Embedded
public class MongoVaultKey {

    @Property
    private PrivateKeyCrytpoAlgorithm algorithm;

    @Property
    private String publicKey;

    @Property
    private String privateKey;

    @Property
    private boolean encrypted;

    @Property
    private Map<String, Object> encryption;

    public PrivateKeyCrytpoAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(PrivateKeyCrytpoAlgorithm algorithm) {
        this.algorithm = algorithm;
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

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public Map<String, Object> getEncryption() {
        return encryption;
    }

    public void setEncryption(Map<String, Object> encryption) {
        this.encryption = encryption;
    }
}
