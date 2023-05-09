package dev.getelements.elements.model.blockchain.wallet;

import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.model.blockchain.BlockchainApi;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.rt.annotation.RemoteModel;
import dev.getelements.elements.rt.annotation.RemoteScope;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

import static dev.getelements.elements.rt.annotation.RemoteScope.API_SCOPE;
import static dev.getelements.elements.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;

@ApiModel
@RemoteModel(
        scopes = {
            @RemoteScope(scope = API_SCOPE, protocol = ELEMENTS_JSON_RPC_PROTOCOL)
        }
)
public class Wallet {

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The system assigned unique id of the wallet.")
    private String id;

    @NotNull
    @ApiModelProperty("The User associated with this wallet.")
    private User user;

    @NotNull
    private Vault vault;

    @NotNull
    @ApiModelProperty("The name given to this wallet.")
    private String displayName;

    @NotNull
    @ApiModelProperty("The protocol used wiht this wallet.")
    private BlockchainApi api;

    @NotNull
    @Size(min = 1)
    @Valid
    @ApiModelProperty("The networks associated with this wallet.")
    private List<BlockchainNetwork> networks;

    @Min(0)
    @ApiModelProperty("The default account. Must not be larger than the count of identities.")
    private int preferredAccount;

    @Valid
    @NotNull
    @Size(min = 1)
    @ApiModelProperty("The list of account pairs included in this wallet.")
    private List<WalletAccount> accounts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Vault getVault() {
        return vault;
    }

    public void setVault(Vault vault) {
        this.vault = vault;
    }

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

    public List<WalletAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<WalletAccount> accounts) {
        this.accounts = accounts;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return getPreferredAccount() == wallet.getPreferredAccount() && Objects.equals(getId(), wallet.getId()) && Objects.equals(getUser(), wallet.getUser()) && Objects.equals(getVault(), wallet.getVault()) && Objects.equals(getDisplayName(), wallet.getDisplayName()) && getApi() == wallet.getApi() && Objects.equals(getNetworks(), wallet.getNetworks()) && Objects.equals(getAccounts(), wallet.getAccounts());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUser(), getVault(), getDisplayName(), getApi(), getNetworks(), getPreferredAccount(), getAccounts());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Wallet{");
        sb.append("id='").append(id).append('\'');
        sb.append(", user=").append(user);
        sb.append(", vault=").append(vault);
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", api=").append(api);
        sb.append(", networks=").append(networks);
        sb.append(", preferredAccount=").append(preferredAccount);
        sb.append(", accounts=").append(accounts);
        sb.append('}');
        return sb.toString();
    }

}
