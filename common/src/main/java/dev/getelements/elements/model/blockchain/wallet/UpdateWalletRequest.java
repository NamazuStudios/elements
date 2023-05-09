package dev.getelements.elements.model.blockchain.wallet;

import dev.getelements.elements.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.rt.annotation.RemoteModel;
import dev.getelements.elements.rt.annotation.RemoteScope;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

import static dev.getelements.elements.rt.annotation.RemoteScope.API_SCOPE;
import static dev.getelements.elements.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;

@ApiModel(description = "Updates a Wallet.")
@RemoteModel(
        scopes = {
                @RemoteScope(scope = API_SCOPE, protocol = ELEMENTS_JSON_RPC_PROTOCOL)
        }
)
public class UpdateWalletRequest {

    @ApiModelProperty("The new display name of the wallet.")
    private String displayName;

    @Min(1)
    @ApiModelProperty("The default identity. Must not be larger than the count of identities.")
    private int preferredAccount;

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

    public List<BlockchainNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(List<BlockchainNetwork> networks) {
        this.networks = networks;
    }

    public int getPreferredAccount() {
        return preferredAccount;
    }

    public void setPreferredAccount(int preferredAccount) {
        this.preferredAccount = preferredAccount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateWalletRequest that = (UpdateWalletRequest) o;
        return getPreferredAccount() == that.getPreferredAccount() && Objects.equals(getDisplayName(), that.getDisplayName()) && Objects.equals(getNetworks(), that.getNetworks());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDisplayName(), getPreferredAccount(), getNetworks());
    }

    @Override
    public String
    toString() {
        final StringBuilder sb = new StringBuilder("UpdateWalletRequest{");
        sb.append("displayName='").append(displayName).append('\'');
        sb.append(", defaultIdentity=").append(preferredAccount);
        sb.append(", networks=").append(networks);
        sb.append('}');
        return sb.toString();
    }

}
