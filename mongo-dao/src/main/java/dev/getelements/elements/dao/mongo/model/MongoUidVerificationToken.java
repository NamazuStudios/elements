package dev.getelements.elements.dao.mongo.model;

import dev.morphia.annotations.*;

import java.sql.Timestamp;

/**
 * MongoDB entity for a single-use email verification token used in the {@code UserUid} verification flow.
 * The {@code token} field doubles as the document {@code _id}.
 *
 * <p>The TTL index on {@code expiry} (with {@code expireAfterSeconds=0}) causes MongoDB to automatically
 * remove expired documents.
 */
@Entity("uid_verification_token")
@Indexes({
    @Index(fields = @Field("user")),
    @Index(fields = @Field(value = "expiry"),
           options = @IndexOptions(expireAfterSeconds = 0))
})
public class MongoUidVerificationToken {

    @Id
    private String token;

    @Reference
    private MongoUser user;

    @Property
    private String scheme;

    @Property
    private String uidId;

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

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getUidId() {
        return uidId;
    }

    public void setUidId(String uidId) {
        this.uidId = uidId;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

}
