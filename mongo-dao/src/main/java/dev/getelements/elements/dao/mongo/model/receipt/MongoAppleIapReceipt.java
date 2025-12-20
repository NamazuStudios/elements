package dev.getelements.elements.dao.mongo.model.receipt;

import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.morphia.annotations.*;

import java.util.Date;
import java.util.Objects;

@Entity(value = "apple_iap_receipt", useDiscriminator = false)
public class MongoAppleIapReceipt {

    @Id
    private String originalTransactionId;

    @Indexed
    @Reference
    private MongoUser user;

    @Property
    private String receiptData;

    @Property
    private int quantity;

    @Property
    private String productId;

    @Property
    private String bundleId;

    @Property
    private Date originalPurchaseDate;

    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoAppleIapReceipt that = (MongoAppleIapReceipt) o;
        return getQuantity() == that.getQuantity() &&
                Objects.equals(getOriginalTransactionId(), that.getOriginalTransactionId()) &&
                Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getReceiptData(), that.getReceiptData()) &&
                Objects.equals(getProductId(), that.getProductId()) &&
                Objects.equals(getBundleId(), that.getBundleId()) &&
                Objects.equals(getOriginalPurchaseDate(), that.getOriginalPurchaseDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOriginalTransactionId(), getUser(), getReceiptData(), getQuantity(), getProductId(), getBundleId(), getOriginalPurchaseDate());
    }

    @Override
    public String toString() {
        return "MongoAppleIapReceipt{" +
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
