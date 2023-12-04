package dev.getelements.elements.model.token;

import dev.getelements.elements.model.profile.Profile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@ApiModel
public class TokenWithExpiration implements Serializable {
    @ApiModelProperty("The id of user")
    @NotNull
    private String userId;
    @ApiModelProperty("The email of the related user")
    @NotNull
    private String email;
    @ApiModelProperty("Expiry time of the token")
    @NotNull
    private Integer expiry;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getExpiry() {
        return expiry;
    }

    public void setExpiry(Integer expiry) {
        this.expiry = expiry;
    }

    public TokenWithExpiration(String userId, String email, Integer expiry) {
        this.userId = userId;
        this.email = email;
        this.expiry = expiry;
    }

    public TokenWithExpiration() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenWithExpiration token = (TokenWithExpiration) o;
        return Objects.equals(userId, token.userId) && Objects.equals(email, token.email) && Objects.equals(expiry, token.expiry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email, expiry);
    }

    @Override
    public String toString() {
        return "Profile{" +
                "userId='" + userId + '\'' +
                ", email=" + email +
                ", expiry=" + expiry +
                '}';
    }
}
