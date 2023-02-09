package com.namazustudios.socialengine.model.blockchain.wallet;

import com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class CreateVaultRequest {

    @NotNull
    @ApiModelProperty("The elements-defined user ID to own the vault.")
    private String userId;

    @NotNull
    @ApiModelProperty(
            "A user-defined name for the vault. This is used simply for the user's reference and has no bearing  on" +
            "the vault's functionality.")
    private String displayName;

    @ApiModelProperty(
            "The passphrase used to to encrypt the vault. If empty, then the vault will not be encrypted. Some " +
            "configurations may opt to disallow encryption entirely."
    )
    private String passphrase;

    @ApiModelProperty(
            "The encryption algorithm used to secure the vault. Once crated, a vault will contains a private/public " +
            "key pair which will be used to encrypt the wallets within the vault."
    )
    private PrivateKeyCrytpoAlgorithm algorithm;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public PrivateKeyCrytpoAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(PrivateKeyCrytpoAlgorithm algorithm) {
        this.algorithm = algorithm;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateVaultRequest that = (CreateVaultRequest) o;
        return Objects.equals(userId, that.userId) && Objects.equals(displayName, that.displayName) && Objects.equals(passphrase, that.passphrase) && algorithm == that.algorithm;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, displayName, passphrase, algorithm);
    }

    @Override
    public String toString() {
        return "CreateVaultRequest{" +
                "userId='" + userId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", passphrase='" + passphrase + '\'' +
                ", algorithm=" + algorithm +
                '}';
    }

}
