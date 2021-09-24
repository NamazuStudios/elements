package com.namazustudios.socialengine.model.user;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.model.profile.Profile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a user in the system.  Users are differing from entrants in that they are active users
 * who have special privilege to create/manage content in the system.
 *
 * Created by patricktwohig on 3/19/15.
 */
@ApiModel
public class User implements Serializable {

    private String id;

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String name;

    @NotNull
    @Pattern(regexp = Constants.Regexp.EMAIL_ADDRESS)
    private String email;

    @NotNull
    private Level level;

    private boolean active;

    private String facebookId;

    private String firebaseId;

    private String appleSignInId;

    private static final User UNPRIVILIGED = new User() {

        @Override
        public String getEmail() {
            return "";
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public Level getLevel() {
            return Level.UNPRIVILEGED;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public String getFirebaseId() {
            return null;
        }

        @Override
        public String getFacebookId() {
            return null;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return  obj == this;
        }

    };

    /**
     * Gets the user's unique ID.
     *
     * @return the user's unique ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the user.
     *
     * @param id the user's unique ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the user's login name.
     *
     * @return
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
    public Level getLevel() {
        return level;
    }

    /**
     * Sets the user's access level.
     *
     * @param level
     */
    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * Returns true if the user is active.
     * @return
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the user active flag.
     *
     * @param active
     */
    public void setActive(boolean active) {
        this.active = active;
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
     *
     * @param appleSignInId
     */
    public void setAppleSignInId(String appleSignInId) {
        this.appleSignInId = appleSignInId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", level=").append(level);
        sb.append(", active=").append(active);
        sb.append(", facebookId='").append(facebookId).append('\'');
        sb.append(", firebaseId='").append(firebaseId).append('\'');
        sb.append(", appleSignInId='").append(appleSignInId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return isActive() == user.isActive() && Objects.equals(getId(), user.getId()) && Objects.equals(getName(), user.getName()) && Objects.equals(getEmail(), user.getEmail()) && getLevel() == user.getLevel() && Objects.equals(getFacebookId(), user.getFacebookId()) && Objects.equals(getFirebaseId(), user.getFirebaseId()) && Objects.equals(getAppleSignInId(), user.getAppleSignInId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getEmail(), getLevel(), isActive(), getFacebookId(), getFirebaseId(), getAppleSignInId());
    }

    /**
     * Gets a special User object which is set to unprivileged.  This is used
     * as a palceholder when a user is not logged in.
     *
     * This is a singleton object.
     *
     * @return an User with the UNPRIVILEGED state set
     */
    public static User getUnprivileged() {
        return UNPRIVILIGED;
    }

    /**
     * Used as the key for the user attribute where appropriate.  This is equivalent
     * to the FQN of the {@link User} class.
     */
    public static final String USER_ATTRIBUTE = User.class.getName();

    public enum Level {

        /**
         * An unprivileged/anonymous user.
         */
        UNPRIVILEGED,

        /**
         * A basic user.
         */
        USER,

        /**
         * An administrator/super user, who can do all of the above as well
         * as delete/create users.
         */
        SUPERUSER,

    }

}
