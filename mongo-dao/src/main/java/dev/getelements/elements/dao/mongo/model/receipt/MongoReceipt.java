package dev.getelements.elements.dao.mongo.model.receipt;

import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.morphia.annotations.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Objects;

@Entity(value = "receipt", useDiscriminator = false)
public class MongoReceipt {

    @Id
    private ObjectId id;

    @Property
    @Indexed
    private String schema;

    @Property
    @Indexed
    private String originalTransactionId;

    @Reference
    @Indexed
    private MongoUser user;

    @Property
    @Indexed
    private Date purchaseTime;

    @Property
    private Document body;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

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

    public Date getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(Date purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public Document getBody() {
        return body;
    }

    public void setBody(Document rawReceipt) {
        this.body = rawReceipt;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MongoReceipt that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(schema, that.schema) && Objects.equals(originalTransactionId, that.originalTransactionId) && Objects.equals(user, that.user) && Objects.equals(purchaseTime, that.purchaseTime) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, schema, originalTransactionId, user, purchaseTime, body);
    }

    @Override
    public String toString() {
        return "MongoReceipt{" +
                "id='" + id + '\'' +
                ", schema='" + schema + '\'' +
                ", originalTransactionId='" + originalTransactionId + '\'' +
                ", user=" + user +
                ", purchaseTime=" + purchaseTime +
                ", rawReceipt=" + body +
                '}';
    }
}
