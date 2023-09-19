package dev.getelements.elements.model.blockchain.contract.near;

import io.swagger.annotations.ApiModelProperty;

public class NearStatus {
    @ApiModelProperty("Failure")
    private Object failure;

    @ApiModelProperty("SuccessValue")
    private NearSuccessValueStatus successValue;

    @ApiModelProperty("SuccessReceiptId")
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
