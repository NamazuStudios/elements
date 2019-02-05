package com.namazustudios.socialengine.model.appleiapreceipt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel
public class CreateAppleIapReceipt {
    @NotNull
    @ApiModelProperty("The base64-encoded string of the raw IAP receipt.")
    private String receiptData;

    @NotNull
    @ApiModelProperty("Whether to point to Apple's Sandbox or Production servers. Value should be either SANDBOX " +
            "or PRODUCTION.")
    private CreateAppleIapReceiptEnvironment createAppleIapReceiptEnvironment;

    public String getReceiptData() {
        return receiptData;
    }

    public void setReceiptData(String receiptData) {
        this.receiptData = receiptData;
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
                ", appleIapVerifyReceiptEnvironment=" + createAppleIapReceiptEnvironment +
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