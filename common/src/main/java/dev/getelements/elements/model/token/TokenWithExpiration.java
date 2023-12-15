package dev.getelements.elements.model.token;

import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class TokenWithExpiration implements Serializable {
    @ApiModelProperty("The id of user")
    @NotNull
    private User user;

    @ApiModelProperty("Expiry time of the token")
    @NotNull
    private Timestamp expiry;

    public User getUserId() {
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

    public TokenWithExpiration(User user, Timestamp expiry) {
        this.user = user;
        this.expiry = expiry;
    }

    public TokenWithExpiration() {}

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
