package dev.getelements.elements.sdk.model.blockchain.contract.near;


import io.swagger.v3.oas.annotations.media.Schema;

/** Represents a NEAR protocol success status that contains a return value. */
public class NearSuccessValueStatus {

    /** Creates a new instance. */
    public NearSuccessValueStatus() {}

    @Schema
    private String successValue;

    /**
     * Returns the success value.
     *
     * @return the success value
     */
    public String getSuccessValue() {
        return successValue;
    }

    /**
     * Sets the success value.
     *
     * @param successValue the success value
     */
    public void setSuccessValue(String successValue) {
        this.successValue = successValue;
    }
}
