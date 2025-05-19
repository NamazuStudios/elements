package dev.getelements.elements.sdk.model.blockchain.contract;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;
import java.util.Objects;

@Schema(description = "Updates a smart contract.")
public class UpdateSmartContractRequest {

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<BlockchainNetwork, SmartContractAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(Map<BlockchainNetwork, SmartContractAddress> addresses) {
        this.addresses = addresses;
    }

    public String getVaultId() {
        return vaultId;
    }

    public void setVaultId(String vaultId) {
        this.vaultId = vaultId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

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
