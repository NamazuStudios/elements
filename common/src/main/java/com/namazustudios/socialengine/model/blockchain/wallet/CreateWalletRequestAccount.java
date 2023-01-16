package com.namazustudios.socialengine.model.blockchain.wallet;

import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import java.util.Objects;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.API_SCOPE;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;

@ApiModel(description = "Specifies a Custodial Wallet Account Creating a Wallet")
@RemoteModel(
        scopes = {
                @RemoteScope(scope = API_SCOPE, protocol = ELEMENTS_JSON_RPC_PROTOCOL)
        }
)
public class CreateWalletRequestAccount {

    public interface Import {}

    public interface Generate {}

    @AssertTrue(groups = Generate.class)
    @AssertFalse(groups = Import.class)
    @ApiModelProperty("Flag which indicates if the account should be generated or imported.")
    private boolean generate;

    @Null(groups = Generate.class)
    @NotNull(groups = Import.class)
    @ApiModelProperty("The Wallet Address - id public identity. Must be null for generated wallets.")
    private String address;

    @Null(groups = Generate.class)
    @ApiModelProperty("The Wallet Account - id private identity. May be null if the wallet is for receive only.")
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
