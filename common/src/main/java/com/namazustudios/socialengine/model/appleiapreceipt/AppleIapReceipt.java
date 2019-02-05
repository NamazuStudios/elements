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
public class AppleIapReceipt {
    @ApiModelProperty("The original transaction identifier of the IAP. We use this as the key for the db object " +
            "as well as the {@link RewardIssuance} context. (For now, we do not persist the transaction_id, only " +
            "the original_transaction_id.)")
    @NotNull(groups={Create.class, Insert.class})
    private String originalTransactionIdentifier;

    @ApiModelProperty("The user submitting the IAP.")
    private User user;

    @ApiModelProperty("The base64-encoded string of the raw IAP receipt. Some, but not all, of the information in " +
            "the receiptData will be unpacked to the other params of this object.")
    @NotNull(groups={Create.class, Insert.class})
    private String receiptData;

    @ApiModelProperty("The number of items the user purchased during the transaction (see iOS' SKPayment.quantity).")
    @NotNull(groups={Create.class, Insert.class})
    private Integer quantity;

    @ApiModelProperty("The product identifier of the purchased item.")
    @NotNull(groups={Create.class, Insert.class})
    private String productIdentifier;

    @ApiModelProperty("The app bundle identifier for the purchased item.")
    @NotNull(groups={Create.class, Insert.class})
    private String bundleIdentifier;

    @ApiModelProperty("The original purchase date in ms. (For now, we do not persist purchase_date_ms, only " +
            "original_purchase_date_ms.)")
    private Long originalPurchaseTimestamp;

    public String getOriginalTransactionIdentifier() {
        return originalTransactionIdentifier;
    }

    public void setOriginalTransactionIdentifier(String originalTransactionIdentifier) {
        this.originalTransactionIdentifier = originalTransactionIdentifier;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getReceiptData() {
        return receiptData;
    }

    public void setReceiptData(String receiptData) {
        this.receiptData = receiptData;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProductIdentifier() {
        return productIdentifier;
    }

    public void setProductIdentifier(String productIdentifier) {
        this.productIdentifier = productIdentifier;
    }

    public Long getOriginalPurchaseTimestamp() {
        return originalPurchaseTimestamp;
    }

    public void setOriginalPurchaseTimestamp(Long originalPurchaseTimestamp) {
        this.originalPurchaseTimestamp = originalPurchaseTimestamp;
    }

    public String getBundleIdentifier() {
        return bundleIdentifier;
    }

    public void setBundleIdentifier(String bundleIdentifier) {
        this.bundleIdentifier = bundleIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppleIapReceipt)) return false;

        AppleIapReceipt appleIapReceipt = (AppleIapReceipt) o;

        if (getOriginalTransactionIdentifier() != null ? !getOriginalTransactionIdentifier().equals(appleIapReceipt.getOriginalTransactionIdentifier()) : appleIapReceipt.getOriginalTransactionIdentifier() != null) return false;
        if (getUser() != null ? !getUser().equals(appleIapReceipt.getUser()) : appleIapReceipt.getUser() != null) return false;
        if (getReceiptData() != null ? !getReceiptData().equals(appleIapReceipt.getReceiptData()) : appleIapReceipt.getReceiptData() != null) return false;
        if (getQuantity() != null ? !getQuantity().equals(appleIapReceipt.getQuantity()) : appleIapReceipt.getQuantity() != null) return false;
        if (getProductIdentifier() != null ? !getProductIdentifier().equals(appleIapReceipt.getProductIdentifier()) : appleIapReceipt.getProductIdentifier() != null) return false;
        if (getBundleIdentifier() != null ? !getBundleIdentifier().equals(appleIapReceipt.getBundleIdentifier()) : appleIapReceipt.getBundleIdentifier() != null) return false;
        return (getOriginalPurchaseTimestamp() != null ? !getOriginalPurchaseTimestamp().equals(appleIapReceipt.getOriginalPurchaseTimestamp()) : appleIapReceipt.getOriginalPurchaseTimestamp() != null);
    }

    @Override
    public int hashCode() {
        int result = getOriginalTransactionIdentifier() != null ? getOriginalTransactionIdentifier().hashCode() : 0;
        result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
        result = 31 * result + (getReceiptData() != null ? getReceiptData().hashCode() : 0);
        result = 31 * result + (getQuantity() != null ? getQuantity().hashCode() : 0);
        result = 31 * result + (getProductIdentifier() != null ? getProductIdentifier().hashCode() : 0);
        result = 31 * result + (getBundleIdentifier() != null ? getBundleIdentifier().hashCode() : 0);
        result = 31 * result + (getOriginalPurchaseTimestamp() != null ? getOriginalPurchaseTimestamp().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AppleIapReceipt{" +
                ", originalTransactionIdentifier='" + originalTransactionIdentifier + '\'' +
                ", user='" + user + '\'' +
                ", receiptData='" + receiptData + '\'' +
                ", quantity='" + quantity + '\'' +
                ", productIdentifier='" + productIdentifier + '\'' +
                ", originalPurchaseTimestamp='" + originalPurchaseTimestamp + '\'' +
                '}';
    }

}