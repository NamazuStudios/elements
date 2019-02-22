package com.namazustudios.socialengine.model.appleiapreceipt;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.profile.Profile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.*;

@ApiModel
public class AppleIapReceipt implements Serializable {
    public static final String ID_TAG_PREFIX = "ID";
    public static final String TAG_SEPARATOR = ".";

    @ApiModelProperty("The original transaction identifier of the IAP. We use this as the key for the db object " +
            "as well as the {@link RewardIssuance} context. (For now, we do not persist the transaction_id, only " +
            "the original_transaction_id.)")
    @NotNull(groups={Create.class, Insert.class})
    private String originalTransactionId;

    @ApiModelProperty("The user submitting the IAP.")
    private User user;

    @ApiModelProperty("The base64-encoded string of the raw IAP receipt. Some, but not all, of the information in " +
            "the receiptData will be unpacked to the other params of this object.")
    @NotNull(groups={Create.class, Insert.class})
    private String receiptData;

    @ApiModelProperty("The number of items the user purchased during the transaction (see iOS' SKPayment.quantity).")
    @NotNull(groups={Create.class, Insert.class})
    @Min(value = 0, message = "Quantity may not be less than 0.")
    private Integer quantity;

    @ApiModelProperty("The product identifier of the purchased item.")
    @NotNull(groups={Create.class, Insert.class})
    private String productId;

    @ApiModelProperty("The app bundle identifier for the purchased item.")
    @NotNull(groups={Create.class, Insert.class})
    private String bundleId;

    @ApiModelProperty("The original purchase date.")
    private Date originalPurchaseDate;

    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public Date getOriginalPurchaseDate() {
        return originalPurchaseDate;
    }

    public void setOriginalPurchaseDate(Date originalPurchaseDate) {
        this.originalPurchaseDate = originalPurchaseDate;
    }

    public static List<String> buildRewardIssuanceTags(final String originalTransactionId, final int skuOrdinal) {
        final List <String> tags = new ArrayList<>();
        tags.add(buildIdentifyingRewardIssuanceTag(originalTransactionId, skuOrdinal));

        return tags;
    }

    public static String buildIdentifyingRewardIssuanceTag(final String originalTransactionId, final int skuOrdinal) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ID_TAG_PREFIX);
        stringBuilder.append(TAG_SEPARATOR);
        stringBuilder.append(originalTransactionId);
        stringBuilder.append(TAG_SEPARATOR);
        stringBuilder.append(skuOrdinal);

        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppleIapReceipt that = (AppleIapReceipt) o;
        return Objects.equals(getOriginalTransactionId(), that.getOriginalTransactionId()) &&
                Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getReceiptData(), that.getReceiptData()) &&
                Objects.equals(getQuantity(), that.getQuantity()) &&
                Objects.equals(getProductId(), that.getProductId()) &&
                Objects.equals(getBundleId(), that.getBundleId()) &&
                Objects.equals(getOriginalPurchaseDate(), that.getOriginalPurchaseDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOriginalTransactionId(), getUser(), getReceiptData(), getQuantity(), getProductId(),
                getBundleId(), getOriginalPurchaseDate());
    }

    @Override
    public String toString() {
        return "AppleIapReceipt{" +
                "originalTransactionId='" + originalTransactionId + '\'' +
                ", user=" + user +
                ", receiptData='" + receiptData + '\'' +
                ", quantity=" + quantity +
                ", productId='" + productId + '\'' +
                ", bundleId='" + bundleId + '\'' +
                ", originalPurchaseDate=" + originalPurchaseDate +
                '}';
    }
}