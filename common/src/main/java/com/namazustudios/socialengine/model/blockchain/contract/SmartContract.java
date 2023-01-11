package com.namazustudios.socialengine.model.blockchain.contract;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;
import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Map;
import java.util.Objects;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.API_SCOPE;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;

@ApiModel(description = "Represents a smart contract.")
@RemoteModel(
        scopes = {
                @RemoteScope(scope = API_SCOPE, protocol = ELEMENTS_JSON_RPC_PROTOCOL)
        }
)
public class SmartContract {

    @Null(groups = ValidationGroups.Insert.class)
    @NotNull(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The Elements database id of the contract.")
    private String id;

    @NotNull
    @ApiModelProperty("The unique symbolic name of the smart contract.")
    private String name;

    @NotNull
    @ApiModelProperty("The name given to this contract for display purposes.")
    private String displayName;

    @NotNull
    @ApiModelProperty(
            "The addresses of the contract from the blockchain. Depending on the network or protocol this " +
            "may have several meanings. For example, this may be the script has for the Ethereum network."
    )
    private Map<BlockchainNetwork, SmartContractAddress> addresses;

    @NotNull
    @ApiModelProperty("The blockchain API used by this wallet.")
    private BlockchainApi api;

    @NotNull
    @ApiModelProperty("The Elements vault used to manage the wallets.")
    private Vault vault;

    @ApiModelProperty("Any metadata for this contract.")
    private Map<String, Object> metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public BlockchainApi getApi() {
        return api;
    }

    public void setApi(BlockchainApi api) {
        this.api = api;
    }

    public Vault getVault() {
        return vault;
    }

    public void setVault(Vault vault) {
        this.vault = vault;
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
        SmartContract that = (SmartContract) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(displayName, that.displayName) && Objects.equals(addresses, that.addresses) && api == that.api && Objects.equals(vault, that.vault) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, displayName, addresses, api, vault, metadata);
    }

    @Override
    public String toString() {
        return "SmartContract{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", addresses=" + addresses +
                ", api=" + api +
                ", vault=" + vault +
                ", metadata=" + metadata +
                '}';
    }

}
