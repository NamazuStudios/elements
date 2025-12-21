package dev.getelements.elements.dao.mongo.model.receipt;

import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Reference;

import java.util.Objects;

@Entity(value = "google_play_iap_receipt", useDiscriminator = false)
public class MongoGooglePlayIapReceipt {

    @Id
    private String orderId;

    @Indexed
    @Reference
    private MongoUser user;

    private String productId;

    private String purchaseToken;

    private int consumptionState;

    private String developerPayload;

    private String kind;

    private int purchaseState;

    private long purchaseTimeMillis;

    private Integer purchaseType;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
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

    public int getConsumptionState() {
        return consumptionState;
    }

    public void setConsumptionState(int consumptionState) {
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

    public int getPurchaseState() {
        return purchaseState;
    }

    public void setPurchaseState(int purchaseState) {
        this.purchaseState = purchaseState;
    }

    public long getPurchaseTimeMillis() {
        return purchaseTimeMillis;
    }

    public void setPurchaseTimeMillis(long purchaseTimeMillis) {
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
        MongoGooglePlayIapReceipt that = (MongoGooglePlayIapReceipt) o;
        return getConsumptionState() == that.getConsumptionState() &&
                getPurchaseState() == that.getPurchaseState() &&
                getPurchaseTimeMillis() == that.getPurchaseTimeMillis() &&
                getPurchaseType() == that.getPurchaseType() &&
                Objects.equals(getOrderId(), that.getOrderId()) &&
                Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getProductId(), that.getProductId()) &&
                Objects.equals(getPurchaseToken(), that.getPurchaseToken()) &&
                Objects.equals(getDeveloperPayload(), that.getDeveloperPayload()) &&
                Objects.equals(getKind(), that.getKind());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrderId(), getUser(), getProductId(), getPurchaseToken(), getConsumptionState(),
                getDeveloperPayload(), getKind(), getPurchaseState(), getPurchaseTimeMillis(), getPurchaseType());
    }

    @Override
    public String toString() {
        return "MongoGooglePlayIapReceipt{" +
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