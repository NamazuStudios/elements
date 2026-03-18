package dev.getelements.elements.sdk.model.appleiapreceipt;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.ValidationGroups.Create;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/** Represents an Apple in-app purchase receipt submitted by a client. */
@Schema
public class AppleIapReceipt implements Serializable {

    /** Tag prefix used when constructing reward issuance tags. */
    public static final String ID_TAG_PREFIX = "ID";

    /** Separator used when constructing reward issuance tags. */
    public static final String TAG_SEPARATOR = ".";

    /** Creates a new instance. */
    public AppleIapReceipt() {}

    @Schema(description =
            "The original transaction identifier of the IAP. We use this as the key for the db object " +
            "as well as the {@link RewardIssuance} context. (For now, we do not persist the transaction_id, only " +
            "the original_transaction_id.)")
    @NotNull(groups={Create.class, Insert.class})
    private String originalTransactionId;

    @Schema(description = "The user submitting the IAP.")
    private User user;

    @Schema(description =
            "The base64-encoded string of the raw IAP receipt. Some, but not all, of the information in " +
            "the receiptData will be unpacked to the other params of this object.")
    @NotNull(groups={Create.class, Insert.class})
    private String receiptData;

    @Schema(description = "The number of items the user purchased during the transaction (see iOS' SKPayment.quantity).")
    @NotNull(groups={Create.class, Insert.class})
    @Min(value = 0, message = "Quantity may not be less than 0.")
    private Integer quantity;

    @Schema(description = "The product identifier of the purchased item.")
    @NotNull(groups={Create.class, Insert.class})
    private String productId;

    @Schema(description = "The app bundle identifier for the purchased item.")
    @NotNull(groups={Create.class, Insert.class})
    private String bundleId;

    @Schema(description = "The original purchase date.")
    private Date originalPurchaseDate;

    /**
     * Returns the original transaction identifier.
     * @return the original transaction identifier
     */
    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    /**
     * Sets the original transaction identifier.
     * @param originalTransactionId the original transaction identifier
     */
    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    /**
     * Returns the user submitting the IAP.
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user submitting the IAP.
     * @param user the user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the base64-encoded raw IAP receipt data.
     * @return the receipt data
     */
    public String getReceiptData() {
        return receiptData;
    }

    /**
     * Sets the base64-encoded raw IAP receipt data.
     * @param receiptData the receipt data
     */
    public void setReceiptData(String receiptData) {
        this.receiptData = receiptData;
    }

    /**
     * Returns the quantity of items purchased.
     * @return the quantity
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of items purchased.
     * @param quantity the quantity
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * Returns the product identifier.
     * @return the product identifier
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets the product identifier.
     * @param productId the product identifier
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Returns the app bundle identifier.
     * @return the bundle identifier
     */
    public String getBundleId() {
        return bundleId;
    }

    /**
     * Sets the app bundle identifier.
     * @param bundleId the bundle identifier
     */
    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    /**
     * Returns the original purchase date.
     * @return the original purchase date
     */
    public Date getOriginalPurchaseDate() {
        return originalPurchaseDate;
    }

    /**
     * Sets the original purchase date.
     * @param originalPurchaseDate the original purchase date
     */
    public void setOriginalPurchaseDate(Date originalPurchaseDate) {
        this.originalPurchaseDate = originalPurchaseDate;
    }

    /**
     * Builds the reward issuance tags for the given transaction and SKU ordinal.
     * @param originalTransactionId the original transaction identifier
     * @param skuOrdinal the SKU ordinal
     * @return the list of reward issuance tags
     */
    public static List<String> buildRewardIssuanceTags(final String originalTransactionId, final int skuOrdinal) {
        final List <String> tags = new ArrayList<>();
        tags.add(buildIdentifyingRewardIssuanceTag(originalTransactionId, skuOrdinal));

        return tags;
    }

    /**
     * Builds the identifying reward issuance tag for the given transaction and SKU ordinal.
     * @param originalTransactionId the original transaction identifier
     * @param skuOrdinal the SKU ordinal
     * @return the identifying reward issuance tag
     */
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