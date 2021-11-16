package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups.Update;
import io.neow3j.wallet.nep6.NEP6Wallet;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@ApiModel(description = "Represents a request to update a Neo Wallet.")
public class UpdateWalletRequest {

    @ApiModelProperty("The new display name of the wallet.")
    @Null(groups = {Update.class})
    private String displayName;

    @ApiModelProperty("The user Id of the current wallet owner. If left null the current logged in user will be assumed to be the wallet owner.")
    @Null(groups = {Update.class})
    private String userId;

    @ApiModelProperty("The user Id of the new wallet owner.")
    @Null(groups = {Update.class})
    private String newUserId;

    @ApiModelProperty("The current password used to log into the wallet.")
    @Null(groups = {Update.class})
    private String password;

    @ApiModelProperty("The new password to be used to encrypt the wallet.")
    @Null(groups = {Update.class})
    private String newPassword;

    private String walletId;

    private String updatedWallet;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNewUserId() {
        return newUserId;
    }

    public void setNewUserId(String newUserId) {
        this.newUserId = newUserId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String newPassword) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getUpdatedWallet() {
        return updatedWallet;
    }

    public void setUpdatedWallet(String updatedWallet) {
        this.updatedWallet = updatedWallet;
    }
}
