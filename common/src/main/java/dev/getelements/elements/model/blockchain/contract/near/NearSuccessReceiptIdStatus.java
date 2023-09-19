package dev.getelements.elements.model.blockchain.contract.near;

import io.swagger.annotations.ApiModelProperty;

public class NearSuccessReceiptIdStatus {
    @ApiModelProperty
    private String successReceiptId;

    public String getSuccessReceiptId() {
        return successReceiptId;
    }

    public void setSuccessReceiptId(String successReceiptId) {
        this.successReceiptId = successReceiptId;
    }
}
