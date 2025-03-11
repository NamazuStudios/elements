package dev.getelements.elements.sdk.model.blockchain.contract.near;


import io.swagger.v3.oas.annotations.media.Schema;

public class NearSuccessReceiptIdStatus {
    @Schema
    private String successReceiptId;

    public String getSuccessReceiptId() {
        return successReceiptId;
    }

    public void setSuccessReceiptId(String successReceiptId) {
        this.successReceiptId = successReceiptId;
    }
}
