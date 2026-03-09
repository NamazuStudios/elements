package dev.getelements.elements.sdk.model.appleiapreceipt;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/** Represents the request body for creating an Apple IAP receipt. */
@Schema
public class CreateAppleIapReceipt implements Serializable {

    /** Creates a new instance. */
    public CreateAppleIapReceipt() {}

    @NotNull
    @Schema(description = "The base64-encoded string of the raw IAP receipt.")
    private String receiptData;

    private CreateAppleIapReceiptEnvironment createAppleIapReceiptEnvironment;

    /**
     * Returns the raw IAP receipt data.
     * @return the receipt data
     */
    public String getReceiptData() {
        return receiptData;
    }

    /**
     * Sets the raw IAP receipt data.
     * @param receiptData the receipt data
     */
    public void setReceiptData(String receiptData) {
        this.receiptData = receiptData;
    }

    /**
     * Returns the environment in which to validate the receipt.
     * @return the environment
     */
    public CreateAppleIapReceiptEnvironment getCreateAppleIapReceiptEnvironment() {
        return createAppleIapReceiptEnvironment;
    }

    /**
     * Sets the environment in which to validate the receipt.
     * @param createAppleIapReceiptEnvironment the environment
     */
    public void setCreateAppleIapReceiptEnvironment(CreateAppleIapReceiptEnvironment createAppleIapReceiptEnvironment) {
        this.createAppleIapReceiptEnvironment = createAppleIapReceiptEnvironment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateAppleIapReceipt that = (CreateAppleIapReceipt) o;
        return Objects.equals(getReceiptData(), that.getReceiptData()) &&
                createAppleIapReceiptEnvironment == that.createAppleIapReceiptEnvironment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReceiptData(), createAppleIapReceiptEnvironment);
    }

    @Override
    public String toString() {
        return "CreateAppleIapReceipt{" +
                "receiptData='" + receiptData + '\'' +
                ", createAppleIapReceiptEnvironment=" + createAppleIapReceiptEnvironment +
                '}';
    }

    /** Specifies the Apple IAP validation environment. */
    public enum CreateAppleIapReceiptEnvironment {
        /**
         * The Sandbox environment (i.e. https://sandbox.itunes.apple.com/verifyReceipt).
         */
        SANDBOX,

        /**
         * The Production environment (i.e. https://buy.itunes.apple.com/verifyReceipt).
         */
        PRODUCTION
    }
}