package dev.getelements.elements.model.blockchain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Result of the invocation response when calling methods on a smart contract.
 */
@ApiModel
@Deprecated
public class InvokeContractResponse {

    @ApiModelProperty("The block which handled the invocation. This is the network's block identifier, whatever that " +
                      "may be. This is not an identifier of an Elements database ID.")
    private String blockNetworkId;

    public String getBlockNetworkId() {
        return blockNetworkId;
    }

    public void setBlockNetworkId(String blockNetworkId) {
        this.blockNetworkId = blockNetworkId;
    }

}
