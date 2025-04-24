package dev.getelements.elements.sdk.model.blockchain.wallet;

import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Objects;

@Schema(description = "Updates a Wallet.")
public class UpdateWalletRequest {

    @Schema(description = "The new display name of the wallet.")
    private String displayName;

    @Min(1)
    @Schema(description = "The default identity. Must not be larger than the count of identities.")
    private int preferredAccount;

    @NotNull
    @Size(min = 1)
    @Schema(description = "The networks associated with this wallet. All must support the Wallet's protocol.")
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
