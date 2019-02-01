package com.namazustudios.socialengine.dao.mongo.model;

import org.mongodb.morphia.annotations.*;

import java.sql.Timestamp;


@Entity(value = "apple_iap_receipt", noClassnameStored = true)
public class MongoAppleIapReceipt {

    @Id
    private String originalTransactionIdentifier;

    @Indexed
    @Reference
    private MongoUser user;

    @Property
    private String receiptData;

    @Property
    private int quantity;

    @Property
    private String productIdentifier;

    @Property
    private String bundleIdentifier;

    @Property
    private Timestamp originalPurchaseTimestamp;

    public String getOriginalTransactionIdentifier() {
        return originalTransactionIdentifier;
    }

    public void setOriginalTransactionIdentifier(String originalTransactionIdentifier) {
        this.originalTransactionIdentifier = originalTransactionIdentifier;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public String getReceiptData() {
        return receiptData;
    }

    public void setReceiptData(String receiptData) {
        this.receiptData = receiptData;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductIdentifier() {
        return productIdentifier;
    }

    public void setProductIdentifier(String productIdentifier) {
        this.productIdentifier = productIdentifier;
    }

    public String getBundleIdentifier() {
        return bundleIdentifier;
    }

    public void setBundleIdentifier(String bundleIdentifier) {
        this.bundleIdentifier = bundleIdentifier;
    }

    public Timestamp getOriginalPurchaseTimestamp() {
        return originalPurchaseTimestamp;
    }

    public void setOriginalPurchaseTimestamp(Timestamp originalPurchaseTimestamp) {
        this.originalPurchaseTimestamp = originalPurchaseTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoAppleIapReceipt)) return false;

        MongoAppleIapReceipt appleIapReceipt = (MongoAppleIapReceipt) o;

        if (getOriginalTransactionIdentifier() != appleIapReceipt.getOriginalTransactionIdentifier()) return false;
        if (getUser() != null ? !getUser().equals(appleIapReceipt.getUser()) : appleIapReceipt.getUser() != null) return false;
        if (getReceiptData() != null ? !getReceiptData().equals(appleIapReceipt.getReceiptData()) : appleIapReceipt.getReceiptData() != null) return false;
        if (getProductIdentifier() != null ? !getProductIdentifier().equals(appleIapReceipt.getProductIdentifier()) : appleIapReceipt.getProductIdentifier() != null) return false;
        if (getBundleIdentifier() != null ? !getBundleIdentifier().equals(appleIapReceipt.getBundleIdentifier()) : appleIapReceipt.getBundleIdentifier() != null) return false;
        if (getQuantity() != appleIapReceipt.getQuantity()) return false;
        if (getOriginalPurchaseTimestamp() != null ? !getOriginalPurchaseTimestamp().equals(appleIapReceipt.getOriginalPurchaseTimestamp()) : appleIapReceipt.getOriginalPurchaseTimestamp() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (getOriginalTransactionIdentifier() != null ? getOriginalTransactionIdentifier().hashCode() : 0);
        result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
        result = 31 * result + (getReceiptData() != null ? getReceiptData().hashCode() : 0);
        result = 31 * result + getQuantity();
        result = 31 * result + (getProductIdentifier() != null ? getProductIdentifier().hashCode() : 0);
        result = 31 * result + (getBundleIdentifier() != null ? getBundleIdentifier().hashCode() : 0);
        result = 31 * result + (getOriginalPurchaseTimestamp() != null ? getOriginalPurchaseTimestamp().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MongoMission{" +
                ", originalTransactionIdentifier='" + originalTransactionIdentifier + '\'' +
                ", user='" + user + '\'' +
                ", receiptData='" + receiptData + '\'' +
                ", quantity='" + quantity + '\'' +
                ", productIdentifier='" + productIdentifier + '\'' +
                ", bundleIdentifier='" + bundleIdentifier + '\'' +
                ", originalPurchaseTimestamp='" + originalPurchaseTimestamp + '\'' +
                '}';
    }

}
