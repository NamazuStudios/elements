package com.namazustudios.socialengine.model.blockchain.contract;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.API_SCOPE;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;

@ApiModel(description = "Creates a smart contract.")
@RemoteModel(
        scopes = {
                @RemoteScope(scope = API_SCOPE, protocol = ELEMENTS_JSON_RPC_PROTOCOL)
        }
)
public class CreateSmartContractRequest {

    @Null(groups = ValidationGroups.Insert.class)
    @NotNull(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The Elements database id of the contract.")
    private String id;

    @NotNull
    @ApiModelProperty("The name given to this contract for display purposes.")
    private String displayName;

    @NotNull
    @ApiModelProperty(
            "The address of the contract from the blockchain. Depending on the network or protocol this " +
            "may have several meanings and vary depending on the specific API or network.")
    private Map<BlockchainNetwork, SmartContractAddress> addresses;

    @NotNull
    @ApiModelProperty("The blockchain API used by this wallet.")
    private BlockchainApi api;

    @NotNull
    @Size(min = 1)
    @ApiModelProperty("The blockchain networks associated with this wallet.")
    private List<BlockchainNetwork> networks;

    @NotNull
    @ApiModelProperty("The Elements database id of the wallet containing the default account to be used for " +
            "contract related requests.")
    private String walletId;

    @ApiModelProperty("Any metadata for this contract.")
    private Map<String, Object> metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
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
        CreateSmartContractRequest that = (CreateSmartContractRequest) o;
        return Objects.equals(id, that.id) && Objects.equals(displayName, that.displayName) && Objects.equals(addresses, that.addresses) && api == that.api && Objects.equals(networks, that.networks) && Objects.equals(walletId, that.walletId) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, addresses, api, networks, walletId, metadata);
    }

    @Override
    public String toString() {
        return "CreateSmartContractRequest{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", addresses=" + addresses +
                ", api=" + api +
                ", networks=" + networks +
                ", walletId='" + walletId + '\'' +
                ", metadata=" + metadata +
                '}';
    }

}
