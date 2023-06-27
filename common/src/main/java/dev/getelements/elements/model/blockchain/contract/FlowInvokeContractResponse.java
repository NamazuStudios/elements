package dev.getelements.elements.model.blockchain.contract;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@ApiModel
public class FlowInvokeContractResponse {

    @ApiModelProperty("The Flow transaction status (See Flow Docs)")
    String status;

    @ApiModelProperty("The Flow transaction status code (See Flow Docs)")
    int statusCode;

    @ApiModelProperty("The Flow transaction error message (See Flow Docs)")
    String errorMessage;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowInvokeContractResponse that = (FlowInvokeContractResponse) o;
        return statusCode == that.statusCode && Objects.equals(status, that.status) && Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, statusCode, errorMessage);
    }

    @Override
    public String toString() {
        return "FlowInvokeContractResponse{" +
                "status='" + status + '\'' +
                ", statusCode=" + statusCode +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

}
