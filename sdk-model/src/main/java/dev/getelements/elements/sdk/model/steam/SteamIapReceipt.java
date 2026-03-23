package dev.getelements.elements.sdk.model.steam;

import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a validated Steam in-app purchase receipt.
 */
@Schema(description =
        "Representation of a validated Steam microtransaction purchase. " +
        "See: https://partner.steamgames.com/doc/webapi/ISteamMicroTxn.")
public class SteamIapReceipt implements Serializable {

    /** Creates a new instance. */
    public SteamIapReceipt() {}

    /** Prefix used when building reward issuance tags by order ID. */
    public static final String ID_TAG_PREFIX = "ID";

    /** Separator character used in reward issuance tags. */
    public static final String TAG_SEPARATOR = ".";

    @Schema(description = "The Steam order ID that uniquely identifies the transaction. Used as the unique key " +
            "for the receipt in the database.")
    @NotNull
    private String orderId;

    @Schema(description = "The Steam internal transaction ID.")
    private String transactionId;

    @Schema(description = "The 64-bit Steam ID of the user who made the purchase.")
    private String steamId;

    @Schema(description = "The Steam AppID for the game.")
    private String appId;

    @Schema(description = "The item ID of the purchased product, taken from the first line item in the transaction.")
    @NotNull
    private String itemId;

    @Schema(description = "The transaction status as reported by Steam (e.g. Committed, Approved, Refunded).")
    private String status;

    @Schema(description = "The ISO 4217 currency code used for the transaction.")
    private String currency;

    @Schema(description = "The time of the purchase in milliseconds since the epoch (Jan 1, 1970).")
    private Long purchaseTime;

    @Schema(description = "The Elements user associated with this purchase.")
    private User user;

    /**
     * Returns the Steam order ID.
     *
     * @return the order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Sets the Steam order ID.
     *
     * @param orderId the order ID
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * Returns the Steam internal transaction ID.
     *
     * @return the transaction ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Sets the Steam internal transaction ID.
     *
     * @param transactionId the transaction ID
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Returns the Steam user ID (64-bit SteamID).
     *
     * @return the Steam ID
     */
    public String getSteamId() {
        return steamId;
    }

    /**
     * Sets the Steam user ID.
     *
     * @param steamId the Steam ID
     */
    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

    /**
     * Returns the Steam AppID.
     *
     * @return the app ID
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Sets the Steam AppID.
     *
     * @param appId the app ID
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * Returns the item ID of the purchased product.
     *
     * @return the item ID
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * Sets the item ID of the purchased product.
     *
     * @param itemId the item ID
     */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    /**
     * Returns the transaction status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the transaction status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the ISO 4217 currency code.
     *
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the ISO 4217 currency code.
     *
     * @param currency the currency
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Returns the purchase time in milliseconds since epoch.
     *
     * @return the purchase time
     */
    public Long getPurchaseTime() {
        return purchaseTime;
    }

    /**
     * Sets the purchase time in milliseconds since epoch.
     *
     * @param purchaseTime the purchase time
     */
    public void setPurchaseTime(Long purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    /**
     * Returns the Elements user associated with this purchase.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the Elements user associated with this purchase.
     *
     * @param user the user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Builds the list of reward issuance tags for the given order ID.
     *
     * @param orderId the order ID
     * @return list of reward issuance tags
     */
    public static List<String> buildRewardIssuanceTags(final String orderId) {
        final List<String> tags = new ArrayList<>();
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
        return ID_TAG_PREFIX + TAG_SEPARATOR + orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SteamIapReceipt that = (SteamIapReceipt) o;
        return Objects.equals(orderId, that.orderId) &&
                Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(steamId, that.steamId) &&
                Objects.equals(appId, that.appId) &&
                Objects.equals(itemId, that.itemId) &&
                Objects.equals(status, that.status) &&
                Objects.equals(currency, that.currency) &&
                Objects.equals(purchaseTime, that.purchaseTime) &&
                Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, transactionId, steamId, appId, itemId, status, currency, purchaseTime, user);
    }

    @Override
    public String toString() {
        return "SteamIapReceipt{" +
                "orderId='" + orderId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", steamId='" + steamId + '\'' +
                ", appId='" + appId + '\'' +
                ", itemId='" + itemId + '\'' +
                ", status='" + status + '\'' +
                ", currency='" + currency + '\'' +
                ", purchaseTime=" + purchaseTime +
                ", user=" + user +
                '}';
    }

}
