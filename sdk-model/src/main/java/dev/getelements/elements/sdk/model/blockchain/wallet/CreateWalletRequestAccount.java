package dev.getelements.elements.sdk.model.blockchain.wallet;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.Objects;

@Schema(description = "Specifies a Custodial Wallet Account Creating a Wallet")
public class CreateWalletRequestAccount {

    public interface Import {}

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

    public boolean isGenerate() {
        return generate;
    }

    public void setGenerate(boolean generate) {
        this.generate = generate;
    }

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
