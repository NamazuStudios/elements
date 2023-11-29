package dev.getelements.elements.dao.mongo.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

@Entity("password_reset_token")
public class MongoPasswordResetToken {
    @Id
    private ObjectId objectId;
    private String userId;
    private String email;
    private int expiry;

    public MongoPasswordResetToken(String userId, String email, int expiry) {
        this.userId = userId;
        this.email = email;
        this.expiry = expiry;
    }
}
