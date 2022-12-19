package com.namazustudios.socialengine.model.blockchain.wallet;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.API_SCOPE;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;

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

    @ApiModelProperty("The Wallet's encryption metadata. This is specific to the encryption type used.")
    private Map<String, Object> encryption;

    @Min(0)
    @ApiModelProperty("The default identity. Must not be larger than the count of identities.")
    private int defaultIdentity;

    @Valid
    @NotNull
    @Size(min = 1)
    @ApiModelProperty("The list of identity pairs included in this wallet.")
    private List<WalletIdentityPair> identities;

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

    public Map<String, Object> getEncryption() {
        return encryption;
    }

    public void setEncryption(Map<String, Object> encryption) {
        this.encryption = encryption;
    }

    public int getDefaultIdentity() {
        return defaultIdentity;
    }

    public void setDefaultIdentity(int defaultIdentity) {
        this.defaultIdentity = defaultIdentity;
    }

    public List<WalletIdentityPair> getIdentities() {
        return identities;
    }

    public void setIdentities(List<WalletIdentityPair> identities) {
        this.identities = identities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return getDefaultIdentity() == wallet.getDefaultIdentity() && Objects.equals(getId(), wallet.getId()) && Objects.equals(getUser(), wallet.getUser()) && Objects.equals(getDisplayName(), wallet.getDisplayName()) && getApi() == wallet.getApi() && Objects.equals(getNetworks(), wallet.getNetworks()) && Objects.equals(getEncryption(), wallet.getEncryption()) && Objects.equals(getIdentities(), wallet.getIdentities());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUser(), getDisplayName(), getApi(), getNetworks(), getEncryption(), getDefaultIdentity(), getIdentities());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Wallet{");
        sb.append("id='").append(id).append('\'');
        sb.append(", user=").append(user);
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", protocol=").append(api);
        sb.append(", networks=").append(networks);
        sb.append(", encryption=").append(encryption);
        sb.append(", defaultIdentity=").append(defaultIdentity);
        sb.append(", identities=").append(identities);
        sb.append('}');
        return sb.toString();
    }

}
