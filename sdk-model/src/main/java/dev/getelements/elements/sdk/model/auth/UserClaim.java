package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.user.User;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Objects;

import static dev.getelements.elements.sdk.model.Constants.Regexp.EMAIL_ADDRESS;

/**
 * Represents a user claim from a JWT request.
 */
public class UserClaim implements Serializable {

    @NotNull(groups = {Insert.class, Update.class})
    private String name;

    @NotNull
    @NotNull(groups = {Insert.class, Update.class})
    @Pattern(regexp = EMAIL_ADDRESS)
    private String email;

    @NotNull
    @NotNull(groups = {Insert.class, Update.class})
    private User.Level level;

    private String externalUserId;

    /**
     * Gets the user's login name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's login name.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's access level.
     * @return
     */
    public User.Level getLevel() {
        return level;
    }

    /**
     * Sets the user's access level.
     *
     * @param level
     */
    public void setLevel(User.Level level) {
        this.level = level;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserClaim userClaim = (UserClaim) o;
        return Objects.equals(getName(), userClaim.getName()) && Objects.equals(getEmail(), userClaim.getEmail()) && getLevel() == userClaim.getLevel() && Objects.equals(getExternalUserId(), userClaim.getExternalUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getEmail(), getLevel(), getExternalUserId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserClaim{");
        sb.append("name='").append(name).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", level=").append(level);
        sb.append(", externalUserId='").append(externalUserId).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
