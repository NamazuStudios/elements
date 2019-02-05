package com.namazustudios.socialengine.model.appleiapreceipt;

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
public class CreateAppleIapReceipt {
    @NotNull(groups = {Create.class, Insert.class})
    @ApiModelProperty("The base64-encoded string of the raw IAP receipt.")
    private String receiptData;

    public String getReceiptData() {
        return receiptData;
    }

    public void setReceiptData(String receiptData) {
        this.receiptData = receiptData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreateAppleIapReceipt)) return false;

        CreateAppleIapReceipt appleIapReceipt = (CreateAppleIapReceipt) o;

        if (getReceiptData() != null ? !getReceiptData().equals(appleIapReceipt.getReceiptData()) : appleIapReceipt.getReceiptData() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getReceiptData() != null ? getReceiptData().hashCode() : 0;
        return result;
    }

    @Override
    public String toString() {
        return "CreateAppleIapReceipt{" +
                ", receiptData='" + receiptData + '\'' +
                '}';
    }

}