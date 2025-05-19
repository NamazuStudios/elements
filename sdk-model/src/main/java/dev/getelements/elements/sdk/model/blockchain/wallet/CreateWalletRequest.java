package dev.getelements.elements.sdk.model.blockchain.wallet;

import dev.getelements.elements.sdk.model.blockchain.BlockchainApi;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Objects;

@Schema(description = "Creates a new custodial wallet.")
public class CreateWalletRequest {

    @NotNull
    @Schema(description = 
            "A user-defined name for the wallet. This is used simply for the user's reference and has no bearing  on" +
            "the wallet's functionality.")
    private String displayName;

    @NotNull
    @Schema(description = "The protocol of this wallet. Once set, this cannot be unset.")
    private BlockchainApi api;

    @NotNull
    @Schema(description = "The networks associated with this wallet. All must support the Wallet's protocol.")
    private List<BlockchainNetwork> networks;

    @Min(0)
    @Schema(description = "The default identity. Must not be larger than the count of identities.")
    private int preferredAccount;

    @NotNull
    @Size(min = 1, max = 25)
    private List<CreateWalletRequestAccount> accounts;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public BlockchainApi getApi() {
        return api;
    }

    public void setApi(BlockchainApi api) {
        this.api = api;
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

    public List<CreateWalletRequestAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<CreateWalletRequestAccount> accounts) {
        this.accounts = accounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateWalletRequest that = (CreateWalletRequest) o;
        return preferredAccount == that.preferredAccount && Objects.equals(displayName, that.displayName) && api == that.api && Objects.equals(networks, that.networks) && Objects.equals(accounts, that.accounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, api, networks, preferredAccount, accounts);
    }

    @Override
    public String toString() {
        return "CreateWalletRequest{" +
                "displayName='" + displayName + '\'' +
                ", api=" + api +
                ", networks=" + networks +
                ", defaultIdentity=" + preferredAccount +
                ", identities=" + accounts +
                '}';
    }

}
