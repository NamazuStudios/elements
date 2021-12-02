package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Represents a request to create a neo wallet.")
public class CreateNeoWalletRequest {

    @ApiModelProperty("A user-defined name for the wallet..")
    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    private String displayName;

    @ApiModelProperty("The elements-defined user ID to own the wallet.")
    private String userId;

    @ApiModelProperty("Password to encrypt the wallet.")
    private String password;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
