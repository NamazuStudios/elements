package dev.getelements.elements.sdk.model.blockchain.wallet;

import dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

/** Represents the cryptographic key pair used to secure a {@link Vault}. */
@Schema
public class VaultKey {

    /** Creates a new instance. */
    public VaultKey() {}

    @NotNull
    @Schema(description = "Specifies the private key encryption algorithm used to secure the vault.")
    private PrivateKeyCrytpoAlgorithm algorithm;

    @NotNull
    @Schema(description = "The public key portion of the vault key.")
    private String publicKey;

    @NotNull
    @Schema(description = "The private key portion of the vault key.")
    private String privateKey;

    @Schema(description = "The flag to indicate if the key is encrypted or not.")
    private boolean encrypted;

    @Schema(description = "The Vault's encryption metadata. This is specific to the encryption type used.")
    private Map<String, Object> encryption;

    /**
     * Returns the private key encryption algorithm used to secure this vault key.
     *
     * @return the algorithm
     */
    public PrivateKeyCrytpoAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the private key encryption algorithm used to secure this vault key.
     *
     * @param algorithm the algorithm
     */
    public void setAlgorithm(PrivateKeyCrytpoAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Returns whether the private key is encrypted.
     *
     * @return true if the key is encrypted
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Sets whether the private key is encrypted.
     *
     * @param encrypted true if the key is encrypted
     */
    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    /**
     * Returns the encryption metadata specific to the encryption type used.
     *
     * @return the encryption metadata map
     */
    public Map<String, Object> getEncryption() {
        return encryption;
    }

    /**
     * Sets the encryption metadata specific to the encryption type used.
     *
     * @param encryption the encryption metadata map
     */
    public void setEncryption(Map<String, Object> encryption) {
        this.encryption = encryption;
    }

    /**
     * Returns the public key portion of this vault key.
     *
     * @return the public key
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Sets the public key portion of this vault key.
     *
     * @param publicKey the public key
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Returns the private key portion of this vault key.
     *
     * @return the private key
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * Sets the private key portion of this vault key.
     *
     * @param privateKey the private key
     */
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VaultKey vaultKey = (VaultKey) o;
        return isEncrypted() == vaultKey.isEncrypted() && getAlgorithm() == vaultKey.getAlgorithm() && Objects.equals(getEncryption(), vaultKey.getEncryption()) && Objects.equals(getPublicKey(), vaultKey.getPublicKey()) && Objects.equals(getPrivateKey(), vaultKey.getPrivateKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAlgorithm(), isEncrypted(), getEncryption(), getPublicKey(), getPrivateKey());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultKey{");
        sb.append("algorithm=").append(algorithm);
        sb.append(", encrypted=").append(encrypted);
        sb.append(", encryption=").append(encryption);
        sb.append(", publicKey='").append(publicKey).append('\'');
        sb.append(", privateKey='").append(privateKey).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
