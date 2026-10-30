package dev.getelements.elements.sdk.model.invite;

import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.sql.Timestamp;

/**
 * Represents a superuser invitation token.
 */
@Schema(description = "A single-use, time-limited token used to invite a user to become a superuser.")
public class SuperUserInvite {

    @Schema(description = "The opaque invitation token.")
    private String token;

    @Schema(description = "The email address invited.")
    private String invitedEmail;

    @Schema(description = "The user who created the invitation.")
    private User invitedBy;

    @Schema(description = "The UTC timestamp after which this token is no longer valid.")
    private Timestamp expiry;

    @Schema(description = "The UTC timestamp when the token was created.")
    private Timestamp createdAt;

    @Schema(description = "The UTC timestamp when the token was redeemed, or null if not redeemed.")
    private Timestamp redeemedAt;

    @Schema(description = "The UTC timestamp when the token was revoked, or null if not revoked.")
    private Timestamp revokedAt;

    @Schema(description = "The status of the invitation.")
    private Status status;

    public enum Status {
        PENDING,
        REDEEMED,
        REVOKED,
        EXPIRED
    }

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

    public User getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(User invitedBy) {
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
