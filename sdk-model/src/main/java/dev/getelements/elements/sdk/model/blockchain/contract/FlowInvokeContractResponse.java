package dev.getelements.elements.sdk.model.blockchain.contract;

import io.swagger.v3.oas.annotations.media.Schema;


import java.util.Objects;

/** Represents the response from invoking a method on a Flow smart contract. */
@Schema
public class FlowInvokeContractResponse {

    /** Creates a new instance. */
    public FlowInvokeContractResponse() {}

    /** The Flow transaction status. */
    @Schema(description = "The Flow transaction status (See Flow Docs)")
    String status;

    /** The Flow transaction status code. */
    @Schema(description = "The Flow transaction status code (See Flow Docs)")
    int statusCode;

    /** The Flow transaction error message. */
    @Schema(description = "The Flow transaction error message (See Flow Docs)")
    String errorMessage;

    /**
     * Returns the Flow transaction status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the Flow transaction status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the Flow transaction status code.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the Flow transaction status code.
     *
     * @param statusCode the status code
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Returns the Flow transaction error message.
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the Flow transaction error message.
     *
     * @param errorMessage the error message
     */
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
