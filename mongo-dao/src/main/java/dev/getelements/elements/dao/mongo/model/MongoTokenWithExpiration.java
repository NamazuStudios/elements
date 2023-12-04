package dev.getelements.elements.dao.mongo.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

@Entity("token_with_expiration")
public class MongoTokenWithExpiration {
    @Id
    private ObjectId objectId;
    @Property
    private String userId;
    @Property
    private String email;
    @Property
    private int expiry;

    public MongoTokenWithExpiration(String userId, String email, int expiry) {
        this.userId = userId;
        this.email = email;
        this.expiry = expiry;
    }

    public MongoTokenWithExpiration() {
    }

    public ObjectId getId() {
        return this.objectId;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public int getExpiry() {
        return expiry;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setExpiry(int expiry) {
        this.expiry = expiry;
    }
}
