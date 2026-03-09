package dev.getelements.elements.sdk.model.token;

import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/** Represents an authentication token paired with its expiration timestamp. */
public class TokenWithExpiration implements Serializable {

    /** Creates a new instance. */
    public TokenWithExpiration() {}

    /**
     * Creates a new instance with the given user and expiry.
     *
     * @param user the user associated with this token
     * @param expiry the expiration timestamp of this token
     */
    public TokenWithExpiration(User user, Timestamp expiry) {
        this.user = user;
        this.expiry = expiry;
    }

    @Schema(description = "The id of user")
    @NotNull
    private User user;

    @Schema(description = "Expiry time of the token")
    @NotNull
    private Timestamp expiry;

    /**
     * Returns the user associated with this token.
     *
     * @return the user
     */
    public User getUserId() {
        return user;
    }

    /**
     * Sets the user associated with this token.
     *
     * @param user the user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the expiration timestamp of this token.
     *
     * @return the expiry timestamp
     */
    public Timestamp getExpiry() {
        return expiry;
    }

    /**
     * Sets the expiration timestamp of this token.
     *
     * @param expiry the expiry timestamp
     */
    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenWithExpiration token = (TokenWithExpiration) o;
        return Objects.equals(user, token.user) && Objects.equals(expiry, token.expiry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, expiry);
    }

    @Override
    public String toString() {
        return "Profile{" +
                "user='" + user + '\'' +
                ", expiry=" + expiry +
                '}';
    }
}
