package com.namazustudios.socialengine.model.blockchain.wallet;

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
            "The passphrase used to to encrypt the vault. If empty, then the vault will not be " +
            "encrypted. Some configurations may opt to disallow encryption entirely.")
    private String passphrase;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateVaultRequest that = (CreateVaultRequest) o;
        return Objects.equals(getDisplayName(), that.getDisplayName()) && Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getPassphrase(), that.getPassphrase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDisplayName(), getUserId(), getPassphrase());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateVaultRequest{");
        sb.append("displayName='").append(displayName).append('\'');
        sb.append(", vaultId='").append(userId).append('\'');
        sb.append(", passphrase='").append(passphrase).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
