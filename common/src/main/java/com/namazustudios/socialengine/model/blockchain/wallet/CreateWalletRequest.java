package com.namazustudios.socialengine.model.blockchain.wallet;

import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
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

@ApiModel(description = "Creates a new custodial wallet.")
@RemoteModel(
        scopes = {
                @RemoteScope(scope = API_SCOPE, protocol = ELEMENTS_JSON_RPC_PROTOCOL)
        }
)
public class CreateWalletRequest {

    @NotNull
    @ApiModelProperty(
            "A user-defined name for the wallet. This is used simply for the user's reference and has no bearing  on" +
            "the wallet's functionality.")
    private String displayName;

    @NotNull
    @ApiModelProperty("The protocol of this wallet. Once set, this cannot be unset.")
    private BlockchainApi api;

    @NotNull
    @ApiModelProperty("The networks associated with this wallet. All must support the Wallet's protocol.")
    private List<BlockchainNetwork> networks;

    @Min(0)
    @ApiModelProperty("The default identity. Must not be larger than the count of identities.")
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
