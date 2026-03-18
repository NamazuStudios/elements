package dev.getelements.elements.sdk.model.blockchain.wallet;

import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.blockchain.BlockchainApi;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Objects;

/** Represents a blockchain wallet associated with a user and a vault. */
@Schema
public class Wallet {

    /** Creates a new instance. */
    public Wallet() {}

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @Schema(description = "The system assigned unique id of the wallet.")
    private String id;

    @NotNull
    @Schema(description = "The User associated with this wallet.")
    private User user;

    @NotNull
    private Vault vault;

    @NotNull
    @Schema(description = "The name given to this wallet.")
    private String displayName;

    @NotNull
    @Schema(description = "The protocol used wiht this wallet.")
    private BlockchainApi api;

    @NotNull
    @Size(min = 1)
    @Valid
    @Schema(description = "The networks associated with this wallet.")
    private List<BlockchainNetwork> networks;

    @Min(0)
    @Schema(description = "The default account. Must not be larger than the count of identities.")
    private int preferredAccount;

    @Valid
    @NotNull
    @Size(min = 1)
    @Schema(description = "The list of account pairs included in this wallet.")
    private List<WalletAccount> accounts;

    /**
     * Returns the system-assigned unique ID of this wallet.
     *
     * @return the wallet ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the system-assigned unique ID of this wallet.
     *
     * @param id the wallet ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the user associated with this wallet.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user associated with this wallet.
     *
     * @param user the user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the vault that secures this wallet's keys.
     *
     * @return the vault
     */
    public Vault getVault() {
        return vault;
    }

    /**
     * Sets the vault that secures this wallet's keys.
     *
     * @param vault the vault
     */
    public void setVault(Vault vault) {
        this.vault = vault;
    }

    /**
     * Returns the display name of this wallet.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of this wallet.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the blockchain API protocol used by this wallet.
     *
     * @return the blockchain API
     */
    public BlockchainApi getApi() {
        return api;
    }

    /**
     * Sets the blockchain API protocol used by this wallet.
     *
     * @param api the blockchain API
     */
    public void setApi(BlockchainApi api) {
        this.api = api;
    }

    /**
     * Returns the list of blockchain networks associated with this wallet.
     *
     * @return the networks
     */
    public List<BlockchainNetwork> getNetworks() {
        return networks;
    }

    /**
     * Sets the list of blockchain networks associated with this wallet.
     *
     * @param networks the networks
     */
    public void setNetworks(List<BlockchainNetwork> networks) {
        this.networks = networks;
    }

    /**
     * Returns the index of the preferred account in this wallet.
     *
     * @return the preferred account index
     */
    public int getPreferredAccount() {
        return preferredAccount;
    }

    /**
     * Sets the index of the preferred account in this wallet.
     *
     * @param preferredAccount the preferred account index
     */
    public void setPreferredAccount(int preferredAccount) {
        this.preferredAccount = preferredAccount;
    }

    /**
     * Returns the list of accounts in this wallet.
     *
     * @return the accounts
     */
    public List<WalletAccount> getAccounts() {
        return accounts;
    }

    /**
     * Sets the list of accounts in this wallet.
     *
     * @param accounts the accounts
     */
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
