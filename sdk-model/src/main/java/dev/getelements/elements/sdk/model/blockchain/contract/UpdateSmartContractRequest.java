package dev.getelements.elements.sdk.model.blockchain.contract;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;
import java.util.Objects;

/** Represents a request to update a smart contract's properties. */
@Schema(description = "Updates a smart contract.")
public class UpdateSmartContractRequest {

    /** Creates a new instance. */
    public UpdateSmartContractRequest() {}

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    @Schema(description = "The unique symbolic name of the smart contract.")
    private String name;

    @NotNull
    @Schema(description = "The name given to this contract for display purposes.")
    private String displayName;

    @NotNull
    @Schema(description =
            "The address of the contract from the blockchain. Depending on the network or protocol this " +
            "may have several meanings and vary depending on the specific API or network.")
    private Map<BlockchainNetwork, SmartContractAddress> addresses;

    @NotNull
    @Schema(description =
            "The Elements database id of the wallet containing the default account to be used for " +
            "contract related requests.")
    private String vaultId;

    @Schema(description = "Any metadata for this contract.")
    private Map<String, Object> metadata;

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
     * Returns the Elements database ID of the vault for contract operations.
     *
     * @return the vault ID
     */
    public String getVaultId() {
        return vaultId;
    }

    /**
     * Sets the Elements database ID of the vault for contract operations.
     *
     * @param vaultId the vault ID
     */
    public void setVaultId(String vaultId) {
        this.vaultId = vaultId;
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
        UpdateSmartContractRequest that = (UpdateSmartContractRequest) o;
        return Objects.equals(name, that.name) && Objects.equals(displayName, that.displayName) && Objects.equals(addresses, that.addresses) && Objects.equals(vaultId, that.vaultId) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, displayName, addresses, vaultId, metadata);
    }

    @Override
    public String toString() {
        return "UpdateSmartContractRequest{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", addresses=" + addresses +
                ", vaultId='" + vaultId + '\'' +
                ", metadata=" + metadata +
                '}';
    }

}
