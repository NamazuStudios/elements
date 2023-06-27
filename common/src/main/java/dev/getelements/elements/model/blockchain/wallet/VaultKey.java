package dev.getelements.elements.model.blockchain.wallet;

import dev.getelements.elements.model.crypto.PrivateKeyCrytpoAlgorithm;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

@ApiModel
public class VaultKey {

    @NotNull
    @ApiModelProperty("Specifies the private key encryption algorithm used to secure the vault.")
    private PrivateKeyCrytpoAlgorithm algorithm;

    @NotNull
    @ApiModelProperty("The public key portion of the vault key.")
    private String publicKey;

    @NotNull
    @ApiModelProperty("The private key portion of the vault key.")
    private String privateKey;

    @ApiModelProperty("The flag to indicate if the key is encrypted or not.")
    private boolean encrypted;

    @ApiModelProperty("The Vault's encryption metadata. This is specific to the encryption type used.")
    private Map<String, Object> encryption;

    public PrivateKeyCrytpoAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(PrivateKeyCrytpoAlgorithm algorithm) {
        this.algorithm = algorithm;
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
