package com.namazustudios.socialengine.model.blockchain.wallet;

import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.API_SCOPE;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;

@ApiModel(description = "Updates a Wallet.")
@RemoteModel(
        scopes = {
                @RemoteScope(scope = API_SCOPE, protocol = ELEMENTS_JSON_RPC_PROTOCOL)
        }
)
public class UpdateWalletRequest {

    @ApiModelProperty("The new display name of the wallet.")
    private String displayName;

    @ApiModelProperty(
            "The user Id of the current wallet owner. If left null the current logged in user will be " +
            "assumed to be the wallet owner. If left blank, no ownership will change. Not all users may change wallet " +
            "ownership in which case the API will reject the request with the appropriate exception. If left null, " +
            "then no changes to the wallet's user will take place.")
    private String userId;

    @ApiModelProperty(
            "The current passphrase for the wallet. If left null, no updates to the passphrase will be made. " +
            "If not-null, then the new password must also not be null.")
    private String password;

    @ApiModelProperty(
            "The updated passphrase for the wallet. If left null, no updates to the passphrase will be made. " +
            "If not-null, then the password must also not be null.")
    private String newPassword;

    @NotNull
    @Size(min = 1)
    @ApiModelProperty("The networks associated with this wallet. All must support the Wallet's protocol.")
    private List<BlockchainNetwork> networks;

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
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassphrase() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public List<BlockchainNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(List<BlockchainNetwork> networks) {
        this.networks = networks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateWalletRequest that = (UpdateWalletRequest) o;
        return Objects.equals(getDisplayName(), that.getDisplayName()) && Objects.equals(getUserId(), that.getUserId()) && Objects.equals(password, that.password) && Objects.equals(newPassword, that.newPassword) && Objects.equals(getNetworks(), that.getNetworks());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDisplayName(), getUserId(), password, newPassword, getNetworks());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpdateWalletRequest{");
        sb.append("displayName='").append(displayName).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", newPassword='").append(newPassword).append('\'');
        sb.append(", networks=").append(networks);
        sb.append('}');
        return sb.toString();
    }

}
