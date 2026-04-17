package dev.getelements.elements.dao.mongo.model.user;

import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.morphia.annotations.*;

import java.sql.Timestamp;

/**
 * MongoDB entity for a single-use password reset token.
 * The {@code token} field doubles as the document {@code _id}.
 *
 * <p>The TTL index on {@code expiry} (with {@code expireAfterSeconds=0}) causes MongoDB to automatically
 * remove expired documents.
 */
@Entity("password_reset_token")
@Indexes({
    @Index(fields = @Field("user")),
    @Index(fields = @Field(value = "expiry"),
           options = @IndexOptions(expireAfterSeconds = 0))
})
public class MongoPasswordResetToken {

    @Id
    private String token;

    @Reference
    private MongoUser user;

    @Property
    private Timestamp expiry;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

}
