package com.namazustudios.socialengine.model.blockchain.wallet;

import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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

    @ApiModelProperty("The elements-defined user ID to own the wallet.")
    private String userId;

    @NotNull
    @ApiModelProperty("The protocol of this wallet. Once set, this cannot be unset.")
    private BlockchainApi protocol;

    @NotNull
    @ApiModelProperty("The networks associated with this wallet. All must support the Wallet's protocol.")
    private List<BlockchainNetwork> networks;

    @NotNull
    @ApiModelProperty(
            "The passphrase used to to encrypt the wallet. If empty, then the wallet will not be " +
            "encrypted. Some configurations may opt to disallow encryption entirely.")
    private String passphrase;

    @Min(0)
    @ApiModelProperty("The default identity. Must not be larger than the count of identities.")
    private int defaultIdentity;

    @Valid
    private List<WalletIdentityPair> identities;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BlockchainApi getProtocol() {
        return protocol;
    }

    public void setProtocol(BlockchainApi protocol) {
        this.protocol = protocol;
    }

    public List<BlockchainNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(List<BlockchainNetwork> networks) {
        this.networks = networks;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
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
        CreateWalletRequest that = (CreateWalletRequest) o;
        return getDefaultIdentity() == that.getDefaultIdentity() && Objects.equals(getDisplayName(), that.getDisplayName()) && Objects.equals(getUserId(), that.getUserId()) && getProtocol() == that.getProtocol() && Objects.equals(getNetworks(), that.getNetworks()) && Objects.equals(getPassphrase(), that.getPassphrase()) && Objects.equals(getIdentities(), that.getIdentities());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDisplayName(), getUserId(), getProtocol(), getNetworks(), getPassphrase(), getDefaultIdentity(), getIdentities());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateWalletRequest{");
        sb.append("displayName='").append(displayName).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", protocol=").append(protocol);
        sb.append(", networks=").append(networks);
        sb.append(", passphrase='").append(passphrase).append('\'');
        sb.append(", defaultIdentity=").append(defaultIdentity);
        sb.append(", identities=").append(identities);
        sb.append('}');
        return sb.toString();
    }

}
