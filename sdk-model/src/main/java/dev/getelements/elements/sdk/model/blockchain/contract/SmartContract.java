package dev.getelements.elements.sdk.model.blockchain.contract;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.blockchain.wallet.Vault;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;

import java.util.Map;
import java.util.Objects;

/** Represents a smart contract registered in the Elements system. */
@Schema(description = "Represents a smart contract.")
public class SmartContract {

    /** Creates a new instance. */
    public SmartContract() {}

    @Null(groups = ValidationGroups.Insert.class)
    @NotNull(groups = ValidationGroups.Update.class)
    @Schema(description = "The Elements database id of the contract.")
    private String id;

    @NotNull
    @Pattern(regexp = Constants.Regexp.WHOLE_WORD_ONLY)
    @Schema(description = "The unique symbolic name of the smart contract.")
    private String name;

    @NotNull
    @Schema(description = "The name given to this contract for display purposes.")
    private String displayName;

    @NotNull
    @Schema(description =
            "The addresses of the contract from the blockchain. Depending on the network or protocol this " +
            "may have several meanings. For example, this may be the script has for the Ethereum network."
    )
    private Map<BlockchainNetwork, SmartContractAddress> addresses;

    @NotNull
    @Schema(description = "The Elements vault used to manage the wallets.")
    private Vault vault;

    @Schema(description = "Any metadata for this contract.")
    private Map<String, Object> metadata;

    /**
     * Returns the Elements database ID of the contract.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the Elements database ID of the contract.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the unique symbolic name of the smart contract.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique symbolic name of the smart contract.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the display name of the smart contract.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of the smart contract.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the blockchain addresses of the contract, keyed by network.
     *
     * @return the addresses
     */
    public Map<BlockchainNetwork, SmartContractAddress> getAddresses() {
        return addresses;
    }

    /**
     * Sets the blockchain addresses of the contract, keyed by network.
     *
     * @param addresses the addresses
     */
    public void setAddresses(Map<BlockchainNetwork, SmartContractAddress> addresses) {
        this.addresses = addresses;
    }

    /**
     * Returns the Elements vault used to manage the wallets.
     *
     * @return the vault
     */
    public Vault getVault() {
        return vault;
    }

    /**
     * Sets the Elements vault used to manage the wallets.
     *
     * @param vault the vault
     */
    public void setVault(Vault vault) {
        this.vault = vault;
    }

    /**
     * Returns any metadata for this contract.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets any metadata for this contract.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmartContract that = (SmartContract) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getDisplayName(), that.getDisplayName()) && Objects.equals(getAddresses(), that.getAddresses()) && Objects.equals(getVault(), that.getVault()) && Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDisplayName(), getAddresses(), getVault(), getMetadata());
    }

    @Override
    public String
    toString() {
        final StringBuilder sb = new StringBuilder("SmartContract{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", addresses=").append(addresses);
        sb.append(", vault=").append(vault);
        sb.append(", metadata=").append(metadata);
        sb.append('}');
        return sb.toString();
    }

}
