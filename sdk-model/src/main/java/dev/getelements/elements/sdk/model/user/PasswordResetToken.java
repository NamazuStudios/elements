package dev.getelements.elements.sdk.model.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.sql.Timestamp;

/**
 * Represents a single-use, time-limited token used to reset a user's password.
 *
 * <p>The token value is a randomly-generated UUID v4 string used as the database primary key.
 */
@Schema(description = "A single-use, time-limited token used to reset a user's password.")
public class PasswordResetToken {

    @Schema(description = "The opaque reset token. A UUID v4 string used as the database primary key "
            + "and embedded in the reset link.")
    private String id;

    @Schema(description = "The user whose password is being reset.")
    private User user;

    @Schema(description = "The UTC timestamp after which this token is no longer valid.")
    private Timestamp expiry;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

}
