package com.namazustudios.socialengine.model.blockchain.contract;

import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.API_SCOPE;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;

@ApiModel(description = "Updates a smart contract.")
@RemoteModel(
        scopes = {
                @RemoteScope(scope = API_SCOPE, protocol = ELEMENTS_JSON_RPC_PROTOCOL)
        }
)
public class UpdateSmartContractRequest {

    @NotNull
    @ApiModelProperty("The name given to this contract for display purposes.")
    private String displayName;

    @NotNull
    @ApiModelProperty(
            "The address of the contract from the blockchain. Depending on the network or protocol this " +
            "may have several meanings and vary depending on the specific API or network.")
    private Map<BlockchainNetwork, SmartContractAddress> addresses;

    @NotNull
    @ApiModelProperty(
            "The Elements database id of the wallet containing the default account to be used for " +
            "contract related requests.")
    private String vaultId;

    @ApiModelProperty("Any metadata for this contract.")
    private Map<String, Object> metadata;

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
        return Objects.equals(displayName, that.displayName) && Objects.equals(addresses, that.addresses) && Objects.equals(vaultId, that.vaultId) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, addresses, vaultId, metadata);
    }

    @Override
    public String toString() {
        return "UpdateSmartContractRequest{" +
                "displayName='" + displayName + '\'' +
                ", addresses=" + addresses +
                ", vaultId='" + vaultId + '\'' +
                ", metadata=" + metadata +
                '}';
    }

}
