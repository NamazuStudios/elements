package dev.getelements.elements.model.blockchain.contract.near;

import io.swagger.annotations.ApiModelProperty;

public class NearSuccessValueStatus {
    @ApiModelProperty
    private String successValue;

    public String getSuccessValue() {
        return successValue;
    }

    public void setSuccessValue(String successValue) {
        this.successValue = successValue;
    }
}
