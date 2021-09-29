package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Represents a request to update a Smart Contract Template.")
public class UpdateWalletRequest {

    @ApiModelProperty("The display name of the wallet.")
    @NotNull(groups = ValidationGroups.Insert.class)
    private String displayName;

    @ApiModelProperty("The password used to log into the wallet.")
    @NotNull(groups = ValidationGroups.Insert.class)
    private String password;

}
