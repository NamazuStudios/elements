package dev.getelements.elements.sdk.model.googleplayiapreceipt;

import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a validated Google Play in-app purchase receipt.
 */
@Schema(description =
        "Representation of a validated Google Play in-app purchase. " +
        "See: https://developers.google.com/android-publisher/api-ref/purchases/products.")
public class GooglePlayIapReceipt implements Serializable {

    /** Creates a new instance. */
    public GooglePlayIapReceipt() {}

    /** Prefix used when building reward issuance tags by order ID. */
    public static final String ID_TAG_PREFIX = "ID";

    /** Separator character used in reward issuance tags. */
    public static final String TAG_SEPARATOR = ".";

    /** Purchase state indicating the item has been purchased. */
    final public static int PURCHASE_STATE_PURCHASED = 0;

    /** Purchase state indicating the purchase has been canceled. */
    final public static int PURCHASE_STATE_CANCELED = 1;

    @Schema(description = "The order id associated with the purchase of the inapp product. This is assumed to be unique " +
            "and is therefore used as the unique key for the receipt in the db.")
    @NotNull
    private String orderId;

    @Schema(description = "The user submitting the IAP.")
    private User user;

    @Schema(description = "The product id purchased by the user.")
    @NotNull
    private String productId;

    @Schema(description = "The purchase token issued to the user upon original purchase.")
    @NotNull
    private String purchaseToken;

    @Schema(description = "The consumption state of the inapp product. Possible values are: 0, Yet to be consumed; 1, " +
            "Consumed.")
    private Integer consumptionState;

    @Schema(description = "A developer-specified string that contains supplemental information about an order.")
    private String developerPayload;

    @Schema(description = "This kind represents an inappPurchase object in the androidpublisher service.")
    private String kind;

    @Schema(description = "The purchase state of the order. Possible values are: 0, Purchased; 1, Canceled.")
    private Integer purchaseState;

    @Schema(description = "The time the product was purchased, in milliseconds since the epoch (Jan 1, 1970).")
    private Long purchaseTimeMillis;

    @Schema(description =
            "The type of purchase of the inapp product. This field is only set if this purchase was not " +
            "made using the standard in-app billing flow. Possible values are: 0, Test (i.e. purchased from a " +
            "license testing account); 1, Promo (i.e. purchased using a promo code).")
    private Integer purchaseType;

    /**
     * Returns the order ID of the purchase.
     *
     * @return the order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Sets the order ID of the purchase.
     *
     * @param orderId the order ID
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * Returns the user who made the purchase.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user who made the purchase.
     *
     * @param user the user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the product ID purchased.
     *
     * @return the product ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets the product ID purchased.
     *
     * @param productId the product ID
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Returns the purchase token.
     *
     * @return the purchase token
     */
    public String getPurchaseToken() {
        return purchaseToken;
    }

    /**
     * Sets the purchase token.
     *
     * @param purchaseToken the purchase token
     */
    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    /**
     * Returns the consumption state.
     *
     * @return the consumption state
     */
    public Integer getConsumptionState() {
        return consumptionState;
    }

    /**
     * Sets the consumption state.
     *
     * @param consumptionState the consumption state
     */
    public void setConsumptionState(Integer consumptionState) {
        this.consumptionState = consumptionState;
    }

    /**
     * Returns the developer payload.
     *
     * @return the developer payload
     */
    public String getDeveloperPayload() {
        return developerPayload;
    }

    /**
     * Sets the developer payload.
     *
     * @param developerPayload the developer payload
     */
    public void setDeveloperPayload(String developerPayload) {
        this.developerPayload = developerPayload;
    }

    /**
     * Returns the kind of the inapp purchase object.
     *
     * @return the kind
     */
    public String getKind() {
        return kind;
    }

    /**
     * Sets the kind of the inapp purchase object.
     *
     * @param kind the kind
     */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * Returns the purchase state.
     *
     * @return the purchase state
     */
    public Integer getPurchaseState() {
        return purchaseState;
    }

    /**
     * Sets the purchase state.
     *
     * @param purchaseState the purchase state
     */
    public void setPurchaseState(Integer purchaseState) {
        this.purchaseState = purchaseState;
    }

    /**
     * Returns the time the product was purchased, in milliseconds since epoch.
     *
     * @return the purchase time in milliseconds
     */
    public Long getPurchaseTimeMillis() {
        return purchaseTimeMillis;
    }

    /**
     * Sets the time the product was purchased, in milliseconds since epoch.
     *
     * @param purchaseTimeMillis the purchase time in milliseconds
     */
    public void setPurchaseTimeMillis(Long purchaseTimeMillis) {
        this.purchaseTimeMillis = purchaseTimeMillis;
    }

    /**
     * Returns the purchase type.
     *
     * @return the purchase type
     */
    public Integer getPurchaseType() {
        return purchaseType;
    }

    /**
     * Sets the purchase type.
     *
     * @param purchaseType the purchase type
     */
    public void setPurchaseType(Integer purchaseType) {
        this.purchaseType = purchaseType;
    }

    /**
     * Builds the list of reward issuance tags for the given order ID.
     *
     * @param orderId the order ID
     * @return list of reward issuance tags
     */
    public static List<String> buildRewardIssuanceTags(final String orderId) {
        final List <String> tags = new ArrayList<>();
        tags.add(buildIdentifyingRewardIssuanceTag(orderId));

        return tags;
    }

    /**
     * Builds the identifying reward issuance tag for the given order ID.
     *
     * @param orderId the order ID
     * @return the identifying reward issuance tag
     */
    public static String buildIdentifyingRewardIssuanceTag(final String orderId) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ID_TAG_PREFIX);
        stringBuilder.append(TAG_SEPARATOR);
        stringBuilder.append(orderId);

        return stringBuilder.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GooglePlayIapReceipt that = (GooglePlayIapReceipt) o;
        return Objects.equals(getOrderId(), that.getOrderId()) &&
                Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getProductId(), that.getProductId()) &&
                Objects.equals(getPurchaseToken(), that.getPurchaseToken()) &&
                Objects.equals(getConsumptionState(), that.getConsumptionState()) &&
                Objects.equals(getDeveloperPayload(), that.getDeveloperPayload()) &&
                Objects.equals(getKind(), that.getKind()) &&
                Objects.equals(getPurchaseState(), that.getPurchaseState()) &&
                Objects.equals(getPurchaseTimeMillis(), that.getPurchaseTimeMillis()) &&
                Objects.equals(getPurchaseType(), that.getPurchaseType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrderId(), getUser(), getProductId(), getPurchaseToken(), getConsumptionState(),
                getDeveloperPayload(), getKind(), getPurchaseState(), getPurchaseTimeMillis(), getPurchaseType());
    }

    @Override
    public String toString() {
        return "GooglePlayIapReceipt{" +
                "orderId='" + orderId + '\'' +
                ", user=" + user +
                ", productId='" + productId + '\'' +
                ", purchaseToken='" + purchaseToken + '\'' +
                ", consumptionState=" + consumptionState +
                ", developerPayload='" + developerPayload + '\'' +
                ", kind='" + kind + '\'' +
                ", purchaseState=" + purchaseState +
                ", purchaseTimeMillis=" + purchaseTimeMillis +
                ", purchaseType=" + purchaseType +
                '}';
    }
}