package dev.getelements.elements.sdk.model.blockchain.wallet;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.Objects;

/** Specifies a Custodial Wallet Account when creating a Wallet. */
@Schema(description = "Specifies a Custodial Wallet Account Creating a Wallet")
public class CreateWalletRequestAccount {

    /** Creates a new instance. */
    public CreateWalletRequestAccount() {}

    /** Validation group indicating the account should be imported. */
    public interface Import {}

    /** Validation group indicating the account should be generated. */
    public interface Generate {}

    @AssertTrue(groups = Generate.class)
    @AssertFalse(groups = Import.class)
    @Schema(description = "Flag which indicates if the account should be generated or imported.")
    private boolean generate;

    @Null(groups = Generate.class)
    @NotNull(groups = Import.class)
    @Schema(description = "The Wallet Address - id public identity. Must be null for generated wallets.")
    private String address;

    @Null(groups = Generate.class)
    @Schema(description = "The Wallet Account - id private identity. May be null if the wallet is for receive only.")
    private String privateKey;

    /**
     * Returns whether this account should be generated.
     *
     * @return true if the account should be generated
     */
    public boolean isGenerate() {
        return generate;
    }

    /**
     * Sets whether this account should be generated.
     *
     * @param generate true if the account should be generated
     */
    public void setGenerate(boolean generate) {
        this.generate = generate;
    }

    /**
     * Returns the wallet address.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the wallet address.
     *
     * @param address the address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns the private key for the account.
     *
     * @return the private key
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * Sets the private key for the account.
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
        CreateWalletRequestAccount that = (CreateWalletRequestAccount) o;
        return generate == that.generate && Objects.equals(address, that.address) && Objects.equals(privateKey, that.privateKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generate, address, privateKey);
    }

    @Override
    public String toString() {
        return "CreateWalletRequestAccount{" +
                "generate=" + generate +
                ", address='" + address + '\'' +
                ", privateKey='" + privateKey + '\'' +
                '}';
    }

}
