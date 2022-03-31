package com.namazustudios.socialengine.model.blockchain.bsc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Represents a request to create a BscToken definition.")
public class MintBscTokenResponse {

    @ApiModelProperty("The token in its post mint attempt state.")
    private BscToken token;

    @ApiModelProperty("The hash of the block that the token was added to, if any.")
    private Long blockIndex;

    public BscToken getToken() {
        return token;
    }

    public void setToken(BscToken token) {
        this.token = token;
    }

    public Long getBlockIndex() {
        return blockIndex;
    }

    public void setBlockIndex(Long blockIndex) {
        this.blockIndex = blockIndex;
    }
}
