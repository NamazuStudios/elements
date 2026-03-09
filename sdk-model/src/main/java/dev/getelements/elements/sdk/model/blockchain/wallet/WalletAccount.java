package dev.getelements.elements.sdk.model.blockchain.wallet;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Represents a single account (address/key pair) within a blockchain {@link Wallet}. */
@Schema
public class WalletAccount {

    /** Creates a new instance. */
    public WalletAccount() {}

    @NotNull
    @Schema(description = "The Wallet Address - id public identity. Required.")
    private String address;

    @Schema(description = "The Wallet Account - id private identity. May be null if the wallet is for receive only.")
    private String privateKey;

    @Schema(description = "Indicates if this identity is encrypted.")
    private boolean encrypted;

    /**
     * Returns the public wallet address for this account.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the public wallet address for this account.
     *
     * @param address the address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns the private key for this account, or null if this is a receive-only wallet.
     *
     * @return the private key
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * Sets the private key for this account.
     *
     * @param privateKey the private key
     */
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Returns whether the private key for this account is encrypted.
     *
     * @return true if the private key is encrypted
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Sets whether the private key for this account is encrypted.
     *
     * @param encrypted true if the private key is encrypted
     */
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
