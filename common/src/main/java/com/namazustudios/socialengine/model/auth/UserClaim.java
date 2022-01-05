package com.namazustudios.socialengine.model.auth;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.user.User;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static com.namazustudios.socialengine.Constants.Regexp.EMAIL_ADDRESS;

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

    private String facebookId;

    private String firebaseId;

    private String appleSignInId;

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

    /**
     * Gets the user's facebook id, if present.  If a user is not linked to
     * a Facebook account then this will simply be null.
     *
     * @return the user's facebook ID
     */
    public String getFacebookId() {
        return facebookId;
    }

    /**
     * Sets a user's facebook id.
     *
     * @param facebookId the user's facebook id
     */
    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    /**
     * Gets the user's firebase ID.
     *
     * @return the user's firebase ID.
     */
    public String getFirebaseId() {
        return firebaseId;
    }

    /**
     * Sets the user's firebase ID.
     *
     * @param firebaseId
     */
    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    /**
     * Gets the Apple sign-in ID.
     *
     * @return the apple sign-in id
     */
    public String getAppleSignInId() {
        return appleSignInId;
    }

    /**
     * Sets the Apple sign-in ID
     *
     * @param appleSignInId
     */
    public void setAppleSignInId(String appleSignInId) {
        this.appleSignInId = appleSignInId;
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
        return Objects.equals(getName(), userClaim.getName()) && Objects.equals(getEmail(), userClaim.getEmail()) && getLevel() == userClaim.getLevel() && Objects.equals(getFacebookId(), userClaim.getFacebookId()) && Objects.equals(getFirebaseId(), userClaim.getFirebaseId()) && Objects.equals(getAppleSignInId(), userClaim.getAppleSignInId()) && Objects.equals(getExternalUserId(), userClaim.getExternalUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getEmail(), getLevel(), getFacebookId(), getFirebaseId(), getAppleSignInId(), getExternalUserId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserClaim{");
        sb.append("name='").append(name).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", level=").append(level);
        sb.append(", facebookId='").append(facebookId).append('\'');
        sb.append(", firebaseId='").append(firebaseId).append('\'');
        sb.append(", appleSignInId='").append(appleSignInId).append('\'');
        sb.append(", externalUserId='").append(externalUserId).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
