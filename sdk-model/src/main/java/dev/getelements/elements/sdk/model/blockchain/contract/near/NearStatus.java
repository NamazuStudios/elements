package dev.getelements.elements.sdk.model.blockchain.contract.near;


import io.swagger.v3.oas.annotations.media.Schema;

public class NearStatus {
    @Schema(description = "Failure")
    private Object failure;

    @Schema(description = "SuccessValue")
    private NearSuccessValueStatus successValue;

    @Schema(description = "SuccessReceiptId")
    private NearSuccessReceiptIdStatus successReceiptId;

    public Object getFailure() {
        return failure;
    }

    public void setFailure(Object failure) {
        this.failure = failure;
    }

    public NearSuccessValueStatus getSuccessValue() {
        return successValue;
    }

    public void setSuccessValue(NearSuccessValueStatus successValue) {
        this.successValue = successValue;
    }

    public NearSuccessReceiptIdStatus getSuccessReceiptId() {
        return successReceiptId;
    }

    public void setSuccessReceiptId(NearSuccessReceiptIdStatus successReceiptId) {
        this.successReceiptId = successReceiptId;
    }
}
