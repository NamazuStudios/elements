package dev.getelements.elements.sdk.model.appleiapreceipt;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Schema
public class CreateAppleIapReceipt implements Serializable {

    @NotNull
    @Schema(description = "The base64-encoded string of the raw IAP receipt.")
    private String receiptData;

    private CreateAppleIapReceiptEnvironment createAppleIapReceiptEnvironment;

    public String getReceiptData() {
        return receiptData;
    }

    public void setReceiptData(String receiptData) {
        this.receiptData = receiptData;
    }

    public CreateAppleIapReceiptEnvironment getCreateAppleIapReceiptEnvironment() {
        return createAppleIapReceiptEnvironment;
    }

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