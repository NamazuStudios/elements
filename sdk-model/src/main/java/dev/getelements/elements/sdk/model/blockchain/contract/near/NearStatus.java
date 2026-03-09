package dev.getelements.elements.sdk.model.blockchain.contract.near;


import io.swagger.v3.oas.annotations.media.Schema;

/** Represents the status of a NEAR protocol transaction or receipt execution. */
public class NearStatus {

    /** Creates a new instance. */
    public NearStatus() {}

    @Schema(description = "Failure")
    private Object failure;

    @Schema(description = "SuccessValue")
    private NearSuccessValueStatus successValue;

    @Schema(description = "SuccessReceiptId")
    private NearSuccessReceiptIdStatus successReceiptId;

    /**
     * Returns the failure object if execution failed, or null if successful.
     *
     * @return the failure object
     */
    public Object getFailure() {
        return failure;
    }

    /**
     * Sets the failure object.
     *
     * @param failure the failure object
     */
    public void setFailure(Object failure) {
        this.failure = failure;
    }

    /**
     * Returns the success value status if execution succeeded with a return value.
     *
     * @return the success value status
     */
    public NearSuccessValueStatus getSuccessValue() {
        return successValue;
    }

    /**
     * Sets the success value status.
     *
     * @param successValue the success value status
     */
    public void setSuccessValue(NearSuccessValueStatus successValue) {
        this.successValue = successValue;
    }

    /**
     * Returns the success receipt ID status if execution created a new receipt.
     *
     * @return the success receipt ID status
     */
    public NearSuccessReceiptIdStatus getSuccessReceiptId() {
        return successReceiptId;
    }

    /**
     * Sets the success receipt ID status.
     *
     * @param successReceiptId the success receipt ID status
     */
    public void setSuccessReceiptId(NearSuccessReceiptIdStatus successReceiptId) {
        this.successReceiptId = successReceiptId;
    }
}
