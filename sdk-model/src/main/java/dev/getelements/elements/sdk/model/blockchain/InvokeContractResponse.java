package dev.getelements.elements.sdk.model.blockchain;

import io.swagger.v3.oas.annotations.media.Schema;


/**
 * Result of the invocation response when calling methods on a smart contract.
 */
@Schema
@Deprecated
public class InvokeContractResponse {

    /** Creates a new instance. */
    public InvokeContractResponse() {}

    @Schema(description = "The block which handled the invocation. This is the network's block identifier, whatever that " +
                      "may be. This is not an identifier of an Elements database ID.")
    private String blockNetworkId;

    /**
     * Returns the block network ID.
     *
     * @return the block network ID
     */
    public String getBlockNetworkId() {
        return blockNetworkId;
    }

    /**
     * Sets the block network ID.
     *
     * @param blockNetworkId the block network ID
     */
    public void setBlockNetworkId(String blockNetworkId) {
        this.blockNetworkId = blockNetworkId;
    }

}
