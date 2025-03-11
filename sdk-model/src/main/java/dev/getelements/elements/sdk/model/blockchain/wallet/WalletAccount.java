package dev.getelements.elements.sdk.model.blockchain.wallet;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Schema
public class WalletAccount {

    @NotNull
    @Schema(description = "The Wallet Address - id public identity. Required.")
    private String address;

    @Schema(description = "The Wallet Account - id private identity. May be null if the wallet is for receive only.")
    private String privateKey;

    @Schema(description = "Indicates if this identity is encrypted.")
    private boolean encrypted;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WalletAccount that = (WalletAccount) o;
        return isEncrypted() == that.isEncrypted() && Objects.equals(getAddress(), that.getAddress()) && Objects.equals(getPrivateKey(), that.getPrivateKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getPrivateKey(), isEncrypted());
    }

    @Override
    public String toString() {
        // This is here deliberately to ensure that the wallet's sensitive contents do not get logged to. Therefore,
        // all fields are skipped except the encrypted flag.
        final StringBuilder sb = new StringBuilder("WalletIdentityPair{");
        sb.append("encrypted=").append(encrypted);
        sb.append('}');
        return sb.toString();
    }

}
