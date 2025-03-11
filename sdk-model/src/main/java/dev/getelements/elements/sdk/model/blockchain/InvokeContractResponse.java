package dev.getelements.elements.sdk.model.blockchain;

import io.swagger.v3.oas.annotations.media.Schema;


/**
 * Result of the invocation response when calling methods on a smart contract.
 */
@Schema
@Deprecated
public class InvokeContractResponse {

    @Schema(description = "The block which handled the invocation. This is the network's block identifier, whatever that " +
                      "may be. This is not an identifier of an Elements database ID.")
    private String blockNetworkId;

    public String getBlockNetworkId() {
        return blockNetworkId;
    }

    public void setBlockNetworkId(String blockNetworkId) {
        this.blockNetworkId = blockNetworkId;
    }

}
