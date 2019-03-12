package com.namazustudios.socialengine.model.googleplayiapreceipt;

import com.namazustudios.socialengine.model.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

@ApiModel(description = "Representation of a validated Google Play in-app purchase. " +
        "See: https://developers.google.com/android-publisher/api-ref/purchases/products.")
public class GooglePlayIapReceipt implements Serializable {
    final public static int PURCHASE_STATE_PURCHASED = 0;
    final public static int PURCHASE_STATE_CANCELED = 1;

    @ApiModelProperty("The order id associated with the purchase of the inapp product. This is assumed to be unique " +
            "and is therefore used as the unique key for the receipt in the db.")
    @NotNull
    private String orderId;

    @ApiModelProperty("The user submitting the IAP.")
    private User user;

    @ApiModelProperty("The product id purchased by the user.")
    @NotNull
    private String productId;

    @ApiModelProperty("The purchase token issued to the user upon original purchase.")
    @NotNull
    private String purchaseToken;

    @ApiModelProperty("The consumption state of the inapp product. Possible values are: 0, Yet to be consumed; 1, " +
            "Consumed.")
    private Integer consumptionState;

    @ApiModelProperty("A developer-specified string that contains supplemental information about an order.")
    private String developerPayload;

    @ApiModelProperty("This kind represents an inappPurchase object in the androidpublisher service.")
    private String kind;

    @ApiModelProperty("The purchase state of the order. Possible values are: 0, Purchased; 1, Canceled.")
    private Integer purchaseState;

    @ApiModelProperty("The time the product was purchased, in milliseconds since the epoch (Jan 1, 1970).")
    private Long purchaseTimeMillis;

    @ApiModelProperty("The type of purchase of the inapp product. This field is only set if this purchase was not " +
            "made using the standard in-app billing flow. Possible values are: 0, Test (i.e. purchased from a " +
            "license testing account); 1, Promo (i.e. purchased using a promo code).")
    private Integer purchaseType;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    public Integer getConsumptionState() {
        return consumptionState;
    }

    public void setConsumptionState(Integer consumptionState) {
        this.consumptionState = consumptionState;
    }

    public String getDeveloperPayload() {
        return developerPayload;
    }

    public void setDeveloperPayload(String developerPayload) {
        this.developerPayload = developerPayload;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Integer getPurchaseState() {
        return purchaseState;
    }

    public void setPurchaseState(Integer purchaseState) {
        this.purchaseState = purchaseState;
    }

    public Long getPurchaseTimeMillis() {
        return purchaseTimeMillis;
    }

    public void setPurchaseTimeMillis(Long purchaseTimeMillis) {
        this.purchaseTimeMillis = purchaseTimeMillis;
    }

    public Integer getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(Integer purchaseType) {
        this.purchaseType = purchaseType;
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