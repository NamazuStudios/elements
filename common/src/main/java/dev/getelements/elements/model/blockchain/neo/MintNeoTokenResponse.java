package dev.getelements.elements.model.blockchain.neo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Represents a request to create a NeoToken definition.")
public class MintNeoTokenResponse {

    @ApiModelProperty("The token in its post mint attempt state.")
    private NeoToken token;

    @ApiModelProperty("The hash of the block that the token was added to, if any.")
    private Long blockIndex;

    public NeoToken getToken() {
        return token;
    }

    public void setToken(NeoToken token) {
        this.token = token;
    }

    public Long getBlockIndex() {
        return blockIndex;
    }

    public void setBlockIndex(Long blockIndex) {
        this.blockIndex = blockIndex;
    }
}
