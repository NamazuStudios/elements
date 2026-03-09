package dev.getelements.elements.sdk.model.blockchain.contract.near;


import io.swagger.v3.oas.annotations.media.Schema;

/** Represents a NEAR protocol success status that contains a receipt ID. */
public class NearSuccessReceiptIdStatus {

    /** Creates a new instance. */
    public NearSuccessReceiptIdStatus() {}

    @Schema
    private String successReceiptId;

    /**
     * Returns the success receipt ID.
     *
     * @return the success receipt ID
     */
    public String getSuccessReceiptId() {
        return successReceiptId;
    }

    /**
     * Sets the success receipt ID.
     *
     * @param successReceiptId the success receipt ID
     */
    public void setSuccessReceiptId(String successReceiptId) {
        this.successReceiptId = successReceiptId;
    }
}
