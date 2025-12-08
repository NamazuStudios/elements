package dev.getelements.elements.dao.mongo.model.receipt;

import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.morphia.annotations.*;
import org.bson.Document;

import java.util.Date;
import java.util.Objects;

@Entity(value = "receipt", useDiscriminator = false)
public class MongoReceipt {

    @Id
    private String id;

    @Property
    @Indexed
    private String scheme;

    @Property
    @Indexed
    private String transactionId;

    @Reference
    @Indexed
    private MongoUser user;

    @Property
    @Indexed
    private Date purchaseTime;

    @Property
    private Document rawReceipt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public Date getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(Date purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public Document getRawReceipt() {
        return rawReceipt;
    }

    public void setRawReceipt(Document rawReceipt) {
        this.rawReceipt = rawReceipt;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MongoReceipt that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(scheme, that.scheme) && Objects.equals(transactionId, that.transactionId) && Objects.equals(user, that.user) && Objects.equals(purchaseTime, that.purchaseTime) && Objects.equals(rawReceipt, that.rawReceipt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, scheme, transactionId, user, purchaseTime, rawReceipt);
    }

    @Override
    public String toString() {
        return "MongoReceipt{" +
                "id='" + id + '\'' +
                ", scheme='" + scheme + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", user=" + user +
                ", purchaseTime=" + purchaseTime +
                ", rawReceipt=" + rawReceipt +
                '}';
    }
}
