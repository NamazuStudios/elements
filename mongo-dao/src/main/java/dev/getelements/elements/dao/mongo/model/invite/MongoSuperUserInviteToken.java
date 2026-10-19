package dev.getelements.elements.dao.mongo.model.invite;

import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.morphia.annotations.*;

import java.sql.Timestamp;

/**
 * MongoDB entity for a single-use superuser invitation token.
 * The {@code token} field doubles as the document {@code _id}.
 *
 * <p>TTL index on {@code expiry} (with {@code expireAfterSeconds=0}) causes MongoDB to automatically
 * remove expired documents.
 */
@Entity("superuser_invite_token")
@Indexes({
    @Index(fields = @Field("invitedEmail")),
    @Index(fields = @Field("invitedBy")),
    @Index(fields = @Field(value = "expiry"), options = @IndexOptions(expireAfterSeconds = 0))
})
public class MongoSuperUserInviteToken {

    @Id
    private String token;

    @Property
    private String invitedEmail;

    @Reference
    private MongoUser invitedBy;

    @Property
    private Timestamp expiry;

    @Property
    private Timestamp createdAt;

    @Property
    private Timestamp redeemedAt;

    @Property
    private Timestamp revokedAt;

    @Property
    private String status;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getInvitedEmail() {
        return invitedEmail;
    }

    public void setInvitedEmail(String invitedEmail) {
        this.invitedEmail = invitedEmail;
    }

    public MongoUser getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(MongoUser invitedBy) {
        this.invitedBy = invitedBy;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getRedeemedAt() {
        return redeemedAt;
    }

    public void setRedeemedAt(Timestamp redeemedAt) {
        this.redeemedAt = redeemedAt;
    }

    public Timestamp getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Timestamp revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
