package com.namazustudios.socialengine.service.appleiap.client.model;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.profile.Profile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.*;

@ApiModel
public class AppleIapVerifyReceiptRequest {
    @NotNull
    @ApiModelProperty("The base64-encoded receipt.")
    private String receiptData;

    @NotNull
    @ApiModelProperty("Whether to point to Apple's Sandbox or Production servers.")
    private AppleIapVerifyReceiptEnvironment appleIapVerifyReceiptEnvironment;

    public String getReceiptData() {
        return receiptData;
    }

    public void setReceiptData(String receiptData) {
        this.receiptData = receiptData;
    }

    public AppleIapVerifyReceiptEnvironment getAppleIapVerifyReceiptEnvironment() {
        return appleIapVerifyReceiptEnvironment;
    }

    public void setAppleIapVerifyReceiptEnvironment(AppleIapVerifyReceiptEnvironment appleIapVerifyReceiptEnvironment) {
        this.appleIapVerifyReceiptEnvironment = appleIapVerifyReceiptEnvironment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppleIapVerifyReceiptRequest that = (AppleIapVerifyReceiptRequest) o;
        return Objects.equals(getReceiptData(), that.getReceiptData()) &&
                getAppleIapVerifyReceiptEnvironment() == that.getAppleIapVerifyReceiptEnvironment();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReceiptData(), getAppleIapVerifyReceiptEnvironment());
    }

    @Override
    public String toString() {
        return "AppleIapVerifyReceiptRequest{" +
                "receiptData='" + receiptData + '\'' +
                ", appleIapVerifyReceiptEnvironment=" + appleIapVerifyReceiptEnvironment +
                '}';
    }

    public enum AppleIapVerifyReceiptEnvironment {
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

