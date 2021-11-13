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

    @ApiModelProperty("The display name of the wallet.")
    @NotNull
    private String displayName;

    @ApiModelProperty("The unique ID of the wallet itself.")
    public String userId;

    @ApiModelProperty("The password used to log into the wallet.")
    private String password;

    @ApiModelProperty("The new password to encrypt the wallet.")
    public String newPassword;

    public String getId() {
        return userId;
    }

    public void setId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
