package dev.getelements.elements.dao.mongo.model.receipt;

import dev.getelements.elements.sdk.model.user.User;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Property;
import dev.morphia.annotations.Reference;

import java.util.Objects;

public class MongoFacebookIapReceipt {

    @Id
    private String purchaseId;

    @Indexed
    @Reference
    private User user;

    @Property
    private String reportingId;

    @Property
    private String sku;

    @Property
    private long grantTime;

    @Property
    private long expirationTime;

    @Property
    private String developerPayload;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
    }

    public String getReportingId() {
        return reportingId;
    }

    public void setReportingId(String reportingId) {
        this.reportingId = reportingId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public long getGrantTime() {
        return grantTime;
    }

    public void setGrantTime(long grantTime) {
        this.grantTime = grantTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String getDeveloperPayload() {
        return developerPayload;
    }

    public void setDeveloperPayload(String developerPayload) {
        this.developerPayload = developerPayload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoFacebookIapReceipt that = (MongoFacebookIapReceipt) o;
        return Objects.equals(getSku(), that.getSku()) &&
                Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getPurchaseId(), that.getPurchaseId()) &&
                Objects.equals(getReportingId(), that.getReportingId()) &&
                Objects.equals(getExpirationTime(), that.getExpirationTime()) &&
                Objects.equals(getDeveloperPayload(), that.getDeveloperPayload());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getPurchaseId(), getSku(), getGrantTime(), getExpirationTime(), getReportingId(), getDeveloperPayload());
    }

    @Override
    public String toString() {
        return "MongoFacebookIapReceipt{" +
                "user=" + user +
                ", purchaseId='" + purchaseId + '\'' +
                ", reportingId='" + reportingId + '\'' +
                ", sku='" + sku + '\'' +
                ", grantTime=" + grantTime +
                ", expirationTime=" + expirationTime +
                ", developerPayload='" + developerPayload + '\'' +
                '}';
    }
}
