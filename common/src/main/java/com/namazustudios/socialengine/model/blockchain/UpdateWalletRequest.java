package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.user.User;
import io.neow3j.wallet.nep6.NEP6Wallet;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@ApiModel(description = "Represents a request to update a Neo Wallet.")
public class UpdateWalletRequest {

    @ApiModelProperty("The unique ID of the wallet itself.")
    @NotNull
    public String id;

    @ApiModelProperty("The display name of the wallet.")
    @NotNull
    private String displayName;

    @ApiModelProperty("The password used to log into the wallet.")
    @NotNull
    private String password;

    @ApiModelProperty("The NEP6 wallet file.")
    @NotNull
    public NEP6Wallet wallet;

    @ApiModelProperty("The User associated with this wallet.")
    @NotNull
    public User user;

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

    public NEP6Wallet getWallet() {
        return wallet;
    }

    public void setWallet(NEP6Wallet wallet) {
        this.wallet = wallet;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
